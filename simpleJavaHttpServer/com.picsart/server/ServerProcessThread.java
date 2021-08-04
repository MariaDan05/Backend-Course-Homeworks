package com.picsart.server;

import java.io.*;
import java.net.Socket;

public class ServerProcessThread extends Thread{
        private Socket socket;
        private BufferedReader in;
        private BufferedWriter out;

        public ServerProcessThread(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            start();
        }
        @Override
        public void run() {
            String word;
            try {
                word = in.readLine();
                try {
                    out.write(word + "\n");
                    out.flush();
                } catch (IOException ignored) {}
                try {
                    while (true) {
                        word = in.readLine();
                        if(word.equals("stop")) {
                            this.downService();
                            break;
                        }
                        System.out.println("Echoing: " + word);
                        for (ServerProcessThread vr : ServerSideMain.serverList) {
                            vr.send(word);
                        }
                    }
                } catch (NullPointerException ignored) {}

            } catch (IOException e) {
                this.downService();
            }
        }

        private void send(String message) {
            try {
                out.write(message + "\n");
                out.flush();
            } catch (IOException ignored) {}

        }

        private void downService() {
            try {
                if(!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                    for (ServerProcessThread vr : ServerSideMain.serverList) {
                        if(vr.equals(this)) vr.interrupt();
                        ServerSideMain.serverList.remove(this);
                    }
                }
            } catch (IOException ignored) {}
        }
    }

