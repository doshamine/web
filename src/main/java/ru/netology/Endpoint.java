package ru.netology;

import java.util.Objects;

public class Endpoint {
    final String method;
    final String route;

    public Endpoint(String method, String route) {
        this.method = method;
        this.route = route;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(method, endpoint.method) && Objects.equals(route, endpoint.route);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, route);
    }
}
