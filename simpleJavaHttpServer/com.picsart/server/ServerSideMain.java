package com.picsart.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerSideMain {
    public static final int PORT = 8008;
    public static LinkedList<ServerProcessThread> serverList = new LinkedList<>();

    public static void main(String[] args) {
        try {
            try (ServerSocket server = new ServerSocket(PORT)) {
                System.out.println("Server Started");
                while (true) {
                    Socket socket = server.accept();
                    RequestHandler.handleRequest(socket);
                    try {
                        serverList.add(new ServerProcessThread(socket));
                    } catch (IOException e) {
                        socket.close();
                    }
                }
            }
        }catch (IOException ignored) {}
    }
}
