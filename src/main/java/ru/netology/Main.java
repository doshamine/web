package ru.netology;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.listen(9999);
    }
}