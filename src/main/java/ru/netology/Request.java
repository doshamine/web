package ru.netology;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[] body;
    private final Map<String, List<String>> postParams;
    private final Map<String, List<String>> parts;

    public Request(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.headers = builder.headers;
        this.queryParams = builder.queryParams;
        this.body = builder.body;
        this.postParams = builder.postParams;
        this.parts = builder.parts;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getParam(String param) {
        return queryParams.get(param);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() { return path; }

    public byte[] getBody() {
        return body;
    }

    public Map<String, List<String>> getParts() {
        return parts;
    }

    public List<String> getPart(String name) {
        return parts.get(name);
    }

    public Map<String, List<String>> getPostParams() {
        return postParams;
    }

    public List<String> getPostParam(String name) {
        return postParams.get(name);
    }

    public static class Builder {
        private String method;
        private String path;
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, String> queryParams =  new HashMap<>();
        private byte[] body;
        private final Map<String, List<String>> postParams = new HashMap<>();
        private final Map<String, List<String>> parts = new HashMap<>();

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

        public Builder queryParam(String name, String value) {
            queryParams.put(name, value);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder postParam(String name, String value) {
            if (!postParams.containsKey(name)) {
                postParams.put(name, new LinkedList<>());
            }
            postParams.get(name).add(value);
            return this;
        }

        public Builder part(String name, String value) {
            if (!parts.containsKey(name)) {
                parts.put(name, new LinkedList<>());
            }
            this.parts.get(name).add(value);
            return this;
        }

        public Request build() {
            return new Request(this);
        }
    }
}
