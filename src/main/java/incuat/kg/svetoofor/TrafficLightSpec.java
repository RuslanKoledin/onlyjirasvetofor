package incuat.kg.svetoofor;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class TrafficLightSpec {

    private WebSocketClient client;

    public void connect(String serverUri, TrafficLightApp app) {
        try {
            client = new WebSocketClient(new URI(serverUri)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to server");
                }

                @Override
                public void onMessage(String message) {
                    // Сообщение от сервера → запускаем метод в JavaFX
                    javafx.application.Platform.runLater(() -> app.handleServerMessage(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from server");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (client != null && client.isOpen()) {
            client.send(message);
        }
    }
}

