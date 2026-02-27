package ru.netology;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    private static final String staticPath = Path.of("src", "main", "resources", "static").toString();

    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler(
          "GET", "/classic.html", (Request request, BufferedOutputStream responseStream) -> {
              try {
                  final var filePath = Path.of(staticPath, "/classic.html");
                  final var mimeType = Files.probeContentType(filePath);
                  final var template = Files.readString(filePath);
                  final var content = template.replace(
                      "{time}",
                      LocalDateTime.now().toString());

                  Response response = new Response.Builder()
                      .status(200).message("OK")
                      .header("Content-Type", mimeType)
                      .header("Content-Length", String.valueOf(content.length()))
                      .header("Connection", "close")
                      .body(content)
                      .build();
                  response.send(responseStream);
              } catch (IOException e) {
                  System.err.println("Ошибка при обработке запроса: " + e.getMessage());
              }
        });

        server.addHandler(
            "GET", "/forms.html", (Request request, BufferedOutputStream responseStream) -> {
                try {
                    final var filePath = Path.of(".", "src/main/resources/static/forms.html");
                    final var mimeType = Files.probeContentType(filePath);
                    final var length = Files.size(filePath);

                    Response response = new Response.Builder()
                        .status(200).message("OK")
                        .header("Content-Type", mimeType)
                        .header("Content-Length", String.valueOf(length))
                        .header("Connection", "close")
                        .body(Files.readString(filePath))
                        .build();
                    response.send(responseStream);
                } catch (IOException e) {
                    System.err.println("Ошибка при обработке запроса: " + e.getMessage());
                }
            });

        server.listen(9999);
    }
  }


