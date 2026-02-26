package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private final ExecutorService executor = Executors.newFixedThreadPool(64);
    private final Map<Endpoint, Handler> handlersMap =  new ConcurrentHashMap<>();

    public void addHandler(String method, String route, Handler handler) {
        handlersMap.put(new Endpoint(method, route), handler);
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                executor.submit(new Connection(serverSocket.accept()));
            }
            executor.shutdown();
        } catch (IOException e) {
            System.err.println("Сбой подключения: " + e.getMessage());
        }
    }

    private class Connection implements Runnable {
        public static final String routeRegex = "^[^?]+";
        public static final String paramRegex = "([^?&\\n\\t\\s]+)=([^&\\n\\t\\s]+)";
        public static final String headerRegex = "([\\w-]+): (.*)";

        private final Socket socket;

        public Connection(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                final var requestLine = in.readLine();
                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    System.err.println("Некорректная строка запроса: " + requestLine);
                    return;
                }
                Request.Builder requestBuilder = new Request.Builder().method(parts[0]);

                Pattern routePattern = Pattern.compile(routeRegex);
                Matcher routeMatcher = routePattern.matcher(parts[1]);
                if (!routeMatcher.find()) {
                    throw new IllegalArgumentException("Некорректный запрос");
                }
                String route = routeMatcher.group();

                Endpoint endpoint = new Endpoint(parts[0], route);

                if (!handlersMap.containsKey(endpoint)) {
                    Response response = new Response.Builder()
                        .status(404)
                        .message("Not Found")
                        .header("Content-Length", "0")
                        .header("Connection", "close")
                        .build();
                    response.send(out);
                    return;
                }

                Handler handler = handlersMap.get(endpoint);

                Pattern paramPattern = Pattern.compile(paramRegex);
                Matcher paramMatcher = paramPattern.matcher(parts[1]);
                while (paramMatcher.find()) {
                    String key = paramMatcher.group(1);
                    String value = paramMatcher.group(2);
                    requestBuilder.param(key, value);
                }

                Pattern headerPattern = Pattern.compile(headerRegex);
                while (true) {
                    String header = in.readLine();
                    Matcher headerMatcher = headerPattern.matcher(header);

                    if (!headerMatcher.find()) {
                        break;
                    }

                    String key = headerMatcher.group(1);
                    String value = headerMatcher.group(2);
                    requestBuilder.header(key, value);
                }

                Request request = requestBuilder.build();
                handler.handle(request, out);
            } catch (IOException e){
                System.err.println("Ошибка обработки запроса: " + e.getMessage());
            }
        }
    }
}
