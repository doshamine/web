package ru.netology;

import org.apache.http.client.utils.URIBuilder;

import java.io.*;
import java.net.URI;

import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection implements Runnable {
    private static final Pattern paramPattern = Pattern.compile("([^?&\\n\\t\\s]+)=([^&\\n\\t\\s]+)");
    public static final Pattern headerPattern = Pattern.compile("([\\w-]+): (.*)");

    public static final int limit = 4096;
    public static final byte[] delimiter = "\r\n".getBytes();
    public static final byte[] doubleDelimiter = "\r\n\r\n".getBytes();

    private final Socket socket;
    private final Map<Endpoint, Handler> handlersMap;
    private final byte[] buffer = new byte[limit];

    public Connection(Socket socket, Map<Endpoint, Handler> handlersMap) {
        this.socket = socket;
        this.handlersMap = handlersMap;
    }

    @Override
    public void run() {
        try (
            final var in = new BufferedInputStream(socket.getInputStream());
            final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            in.mark(limit);
            final var read = in.read(buffer);

            final var requestLineEnd = indexOf(buffer, delimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

            if (requestLine.length != 3) {
                badRequest(out);
                return;
            }

            final String method = requestLine[0];
            String url = requestLine[1];
            URI uri = new URIBuilder(url).build();
            String path = uri.getPath();
            String query = uri.getQuery();

            Request.Builder requestBuilder = new Request.Builder()
                .method(method)
                .path(path);

            Endpoint endpoint = new Endpoint(method, path);

            if (!handlersMap.containsKey(endpoint)) {
                notFound(out);
                return;
            }

            Handler handler = handlersMap.get(endpoint);

            if (query != null) {
                setQueryParams(query, requestBuilder);
            }

            final var headersStart = requestLineEnd + delimiter.length;
            final var headersEnd = indexOf(buffer, doubleDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }

            final var headers = getHeaders(in, headersStart, headersEnd);

            setHeaders(headers, requestBuilder);
            in.skip(doubleDelimiter.length);

            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var contentType = extractHeader(headers, "Content-Type");
                String body = getBody(in, Integer.parseInt(contentLength.get()));

                if (contentType.isPresent()) {
                    if (contentType.get().contains("x-www-form-urlencoded")) {
                        setPostParams(body, requestBuilder);
                    }
                }
                requestBuilder.body(body);
            }

            Request request = requestBuilder.build();
            handler.handle(request, out);
        } catch (IOException e){
            System.err.println("Ошибка обработки запроса: " + e.getMessage());
        } catch (URISyntaxException e) {
            System.err.println("Неверный URI: " + e.getMessage());
        }
    }

    private void setPostParams(String body, Request.Builder requestBuilder) {
        String postParams = URLDecoder.decode(body, StandardCharsets.UTF_8);
        Matcher paramMatcher = paramPattern.matcher(postParams);

        while (paramMatcher.find()) {
            String key = paramMatcher.group(1);
            String value = paramMatcher.group(2);
            requestBuilder.postParam(key, value);
        }
    }

    private void setHeaders(String[] headers, Request.Builder requestBuilder) {
        for (String header : headers) {
            Matcher headerMatcher = headerPattern.matcher(header);

            if (!headerMatcher.find()) {
                break;
            }

            String key = headerMatcher.group(1);
            String value = headerMatcher.group(2);
            requestBuilder.header(key, value);
        }
    }

    private void setQueryParams(String query, Request.Builder requestBuilder) {
        Matcher paramMatcher = paramPattern.matcher(query);

        while (paramMatcher.find()) {
            String key = paramMatcher.group(1);
            String value = paramMatcher.group(2);
            requestBuilder.queryParam(key, value);
        }
    }

    private String[] getHeaders(BufferedInputStream in, int headersStart, int headersEnd) throws IOException {
        in.reset();
        in.skip(headersStart);
        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        return new String(headersBytes).split(new String(delimiter));
    }

    private String getBody(BufferedInputStream in, int bodyLength) throws IOException {
        final var bodyBytes = in.readNBytes(bodyLength);
        return new String(bodyBytes);
    }

    private Optional<String> extractHeader(String[] headers, String header) {
        return Arrays.stream(headers)
            .filter(o -> o.startsWith(header))
            .map(o -> o.substring(o.indexOf(" ")))
            .map(String::trim)
            .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        Response response = new Response.Builder()
            .status(400)
            .message("Bad Request")
            .header("Content-Length", "0")
            .header("Connection", "close")
            .build();
        response.send(out);
    }

    private static void notFound(BufferedOutputStream out) throws IOException {
        Response response = new Response.Builder()
            .status(404)
            .message("Not Found")
            .header("Content-Length", "0")
            .header("Connection", "close")
            .build();
        response.send(out);
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
