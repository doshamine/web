package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Response {
    public static final String CRLF = "\r\n";
    private final String httpVersion = "HTTP/1.1";
    private final int status;
    private final String message;
    private final Map<String, String> headers;
    private final String body;

    public Response(Builder builder) {
        this.status = builder.status;
        this.message = builder.message;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public void send(BufferedOutputStream out) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(httpVersion).append(" ")
            .append(status).append(" ")
            .append(message).append(CRLF);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append(CRLF);
        }
        builder.append(CRLF);

        if (body != null) {
            builder.append(body);
            builder.append(CRLF);
        }

        out.write(builder.toString().getBytes());
        out.flush();
    }

    public static class Builder {
        private int status;
        private String message;
        private Map<String, String> headers = new HashMap<>();
        private String body;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
