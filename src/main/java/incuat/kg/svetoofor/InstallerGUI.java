package incuat.kg.svetoofor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * GUI установщик для клиента светофора
 * Создает client.properties и настраивает автозапуск
 */
public class InstallerGUI extends Application {

    private TextField serverIpField;
    private TextField serverPortField;
    private TextField adminLoginField;
    private PasswordField adminPasswordField;
    private CheckBox autoStartCheckBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Traffic Light Client - Установка");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Заголовок
        Label titleLabel = new Label("Настройка клиента светофора");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // IP сервера
        Label serverIpLabel = new Label("IP сервера:");
        grid.add(serverIpLabel, 0, 1);
        serverIpField = new TextField("10.10.90.170");
        grid.add(serverIpField, 1, 1);

        // Порт сервера
        Label serverPortLabel = new Label("Порт:");
        grid.add(serverPortLabel, 0, 2);
        serverPortField = new TextField("52521");
        grid.add(serverPortField, 1, 2);

        // Логин админа (опционально)
        Label adminLoginLabel = new Label("Логин админа (опционально):");
        grid.add(adminLoginLabel, 0, 3);
        adminLoginField = new TextField();
        adminLoginField.setPromptText("Оставьте пустым для специалиста");
        grid.add(adminLoginField, 1, 3);

        // Пароль админа (опционально)
        Label adminPasswordLabel = new Label("Пароль админа (опционально):");
        grid.add(adminPasswordLabel, 0, 4);
        adminPasswordField = new PasswordField();
        adminPasswordField.setPromptText("Оставьте пустым для специалиста");
        grid.add(adminPasswordField, 1, 4);

        // Автозапуск
        autoStartCheckBox = new CheckBox("Добавить в автозагрузку Windows");
        autoStartCheckBox.setSelected(true);
        grid.add(autoStartCheckBox, 0, 5, 2, 1);

        // Кнопки
        Button installButton = new Button("Установить");
        installButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        installButton.setPrefWidth(100);

        Button cancelButton = new Button("Отмена");
        cancelButton.setPrefWidth(100);

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(installButton, cancelButton);
        grid.add(buttonBox, 0, 6, 2, 1);

        // Обработчики
        installButton.setOnAction(e -> {
            try {
                performInstallation();
                showSuccessDialog();
                primaryStage.close();
            } catch (Exception ex) {
                showErrorDialog(ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> {
            primaryStage.close();
            System.exit(0);
        });

        Scene scene = new Scene(grid, 450, 350);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void performInstallation() throws Exception {
        String serverIp = serverIpField.getText().trim();
        String serverPort = serverPortField.getText().trim();
        String adminLogin = adminLoginField.getText().trim();
        String adminPassword = adminPasswordField.getText().trim();

        // Валидация
        if (serverIp.isEmpty()) {
            throw new Exception("IP сервера не может быть пустым");
        }
        if (serverPort.isEmpty()) {
            throw new Exception("Порт не может быть пустым");
        }
        try {
            Integer.parseInt(serverPort);
        } catch (NumberFormatException e) {
            throw new Exception("Порт должен быть числом");
        }

        // Создаем client.properties
        Properties props = new Properties();
        props.setProperty("server.address", serverIp);
        props.setProperty("server.port", serverPort);

        if (!adminLogin.isEmpty()) {
            props.setProperty("admin.login", adminLogin);
        }
        if (!adminPassword.isEmpty()) {
            props.setProperty("admin.password", adminPassword);
        }

        // Определяем путь для сохранения конфига
        String appDir = getInstallationDirectory();
        Path configPath = Paths.get(appDir, "client.properties");

        // Создаем директорию если не существует
        Files.createDirectories(configPath.getParent());

        // Сохраняем конфигурацию
        try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
            props.store(fos, "Traffic Light Client Configuration");
        }

        System.out.println("Configuration saved to: " + configPath);

        // Добавляем в автозагрузку
        if (autoStartCheckBox.isSelected()) {
            addToWindowsStartup();
        }
    }

    private String getInstallationDirectory() {
        // Для jpackage это будет директория приложения
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "AppData", "Local", "TrafficLightClient").toString();
    }

    private void addToWindowsStartup() {
        try {
            String appPath = System.getProperty("user.dir");
            String exePath = Paths.get(appPath, "TrafficLightClient.exe").toString();

            // Создаем VBS скрипт для автозапуска (скрывает консоль)
            String startupFolder = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            Path vbsPath = Paths.get(startupFolder, "TrafficLightClient.vbs");

            String vbsContent = String.format(
                    "Set WshShell = CreateObject(\"WScript.Shell\")\n" +
                            "WshShell.Run chr(34) & \"%s\" & Chr(34), 0\n" +
                            "Set WshShell = Nothing",
                    exePath
            );

            Files.write(vbsPath, vbsContent.getBytes());
            System.out.println("Added to Windows startup: " + vbsPath);

        } catch (IOException e) {
            System.err.println("Failed to add to startup: " + e.getMessage());
        }
    }

    private void showSuccessDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Установка завершена");
        alert.setHeaderText(null);
        alert.setContentText("Клиент светофора успешно настроен!\n\n" +
                "Приложение будет запущено автоматически.");
        alert.showAndWait();

        // Запускаем клиент
        launchClient();
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка установки");
        alert.setHeaderText(null);
        alert.setContentText("Ошибка: " + message);
        alert.showAndWait();
    }

    private void launchClient() {
        try {
            // Запускаем основное приложение
            new Thread(() -> TrafficLightApp.launch(TrafficLightApp.class)).start();
        } catch (Exception e) {
            System.err.println("Failed to launch client: " + e.getMessage());
        }
    }
}
