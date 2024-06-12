package com.seojjun.chatting_program.Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("ALL")
public class ServerApplication extends Application {
    static ArrayList<Map> msgBox = new ArrayList<>();
    public static ExecutorService threadPool;
    public static Vector<Client> clients = new Vector<>();

    ServerSocket serverSocket;

    public void startServer(String IP, int port) {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(IP, port));
        } catch (Exception e) {
            e.printStackTrace();

            if (!serverSocket.isClosed()) {
                stopServer();
            }
            return;
        }

        Runnable thread = () -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    clients.add(new Client(socket));
                    System.out.println("[클라이언트 접속] "
                            + socket.getRemoteSocketAddress()
                            + ": " + Thread.currentThread().getName());
                } catch (Exception e) {
                    if (!serverSocket.isClosed()) {
                        stopServer();
                    }
                    break;
                }
            }
        };
        threadPool = Executors.newCachedThreadPool();
        threadPool.submit(thread);
    }

    public void stopServer() {
        try {
            Iterator<Client> iterator = clients.iterator();

            while (iterator.hasNext()) {
                Client client = iterator.next();
                client.socket.close();
                iterator.remove();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void jsonFileWrite(String message) {
        LocalDateTime now = LocalDateTime.now();
        String formatedNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String[] splitMessage = message.split(":", 2);

        Map<String, Object> obj = new HashMap<>();
        obj.put("date", formatedNow);
        obj.put("userName", splitMessage[0]);
        obj.put("message", splitMessage[1]);
        msgBox.add(obj);

        String dirName = System.getProperty("user.name");

        try (FileWriter file = new FileWriter("C:/Users/" + dirName + "/Documents/chatting_log.json")) {
            Map<String, Object> data = new HashMap<>();
            data.put("data", msgBox);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("나눔고딕", 15));
        root.setCenter(textArea);

        Button toggleButton = new Button("시작하기");
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
        root.setBottom(toggleButton);

        String IP = "127.0.0.1";
        int port = 9876;

        toggleButton.setOnAction(event -> {
            if (toggleButton.getText().equals("시작하기")) {
                startServer(IP, port);
                Platform.runLater(() -> {
                    String message = String.format("[서버 시작]\n", IP, port);
                    textArea.appendText(message);
                    toggleButton.setText("종료하기");
                });
            } else {
                stopServer();
                Platform.runLater(() -> {
                    String message = String.format("[서버 종료]\n", IP, port);
                    textArea.appendText(message);
                    toggleButton.setText("시작하기");
                });
            }
        });

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("[ 채팅 서버 ]");
        primaryStage.setOnCloseRequest(event -> stopServer());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}