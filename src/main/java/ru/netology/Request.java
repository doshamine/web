package ru.netology;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> params;
    private final String body;

    public Request(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.headers = builder.headers;
        this.params = builder.params;
        this.body = builder.body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getParam(String param) {
        return params.get(param);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() { return path; }

    public String getBody() {
        return body;
    }

    public static class Builder {
        private String method;
        private String path;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> params =  new HashMap<>();
        private String body;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder param(String name, String value) {
            params.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Request build() {
            return new Request(this);
        }
    }
}
