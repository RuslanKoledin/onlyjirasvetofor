package incuat.kg.svetoofor;


import javafx.application.Platform;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TrafficLightServer extends WebSocketServer {

    private final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());
    private TrafficLightApp app;

    public TrafficLightServer(int port) {
        super(new InetSocketAddress(port));
    }

    public void setApp(TrafficLightApp app) {
        this.app = app;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Получено сообщение: " + message);

        // Рассылаем всем подключенным клиентам
        synchronized (clients) {
            for (WebSocket client : clients) {
                client.send(message);
            }
        }

        // Вызываем обработчик в TrafficLightApp (в UI потоке JavaFX)
        if (app != null) {
            Platform.runLater(() -> app.handleServerMessage(message));
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server started on port " + getPort());
    }

    public void broadcast(String message) {
        System.out.println("Рассылка сообщения: " + message);
        synchronized (clients) {
            for (WebSocket client : clients) {
                client.send(message);
            }
        }

        // Также вызываем локальный обработчик
        if (app != null) {
            Platform.runLater(() -> app.handleServerMessage(message));
        }
    }

    public static void main(String[] args) {
        TrafficLightServer server = new TrafficLightServer(52521); // порт 52521
        server.start();
    }
}