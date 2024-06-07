package com.seojjun.chatting_program;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("ALL")
public class Client {
    Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
        receive();
    }

    public void receive() {
        Runnable thread = () -> {
            try {
                while (true) {
                    InputStream in = socket.getInputStream();
                    byte[] buffer = new byte[512];

                    int length = in.read(buffer);
                    if (length == -1) throw new IOException();

                    System.out.println("[메시지 수신 성공]"
                            + socket.getRemoteSocketAddress()
                            + ": " + Thread.currentThread().getName());

                    String message = new String(buffer, 0, length, StandardCharsets.UTF_8);
                    for (Client client : ServerApplication.clients) {
                        client.send(message);
                    }
                }
            } catch (Exception e) {
                try {
                    System.out.println("[메시지 수신 오류]"
                            + socket.getRemoteSocketAddress()
                            + ": " + Thread.currentThread().getName());
                    ServerApplication.clients.remove(Client.this);
                    socket.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        };
        ServerApplication.threadPool.submit(thread);
    }

    public void send(String message) {
        Runnable thread = () -> {
            try {
                OutputStream out = socket.getOutputStream();
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                out.write(buffer);
                out.flush();
            } catch (Exception e) {
                try {
                    System.out.println("[메시지 송신 오류]"
                            + socket.getRemoteSocketAddress()
                            + ": " + Thread.currentThread().getName());
                    ServerApplication.clients.remove(Client.this);
                    socket.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        };
        ServerApplication.threadPool.submit(thread);
    }
}
