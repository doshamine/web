package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private final ExecutorService executor = Executors.newFixedThreadPool(64);
    private final Map<Endpoint, Handler> handlersMap =  new ConcurrentHashMap<>();

    public void addHandler(String method, String route, Handler handler) {
        handlersMap.put(new Endpoint(method, route), handler);
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                executor.submit(new Connection(serverSocket.accept(), handlersMap));
            }
            executor.shutdown();
        } catch (IOException e) {
            System.err.println("Сбой подключения: " + e.getMessage());
        }
    }
}
