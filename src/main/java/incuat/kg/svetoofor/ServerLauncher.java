package incuat.kg.svetoofor;

import incuat.kg.svetoofor.jira.JiraClient;
import incuat.kg.svetoofor.jira.JiraPoller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Headless launcher для сервера светофора
 * Запускает WebSocket сервер на порту 52521 и JIRA интеграцию
 */
public class ServerLauncher {
    private static final int DEFAULT_PORT = 52521;

    public static void main(String[] args) {
        System.out.println("=== Traffic Light Server (JIRA Only) ===");

        // Загружаем конфигурацию
        Properties config = loadConfig();
        int port = Integer.parseInt(config.getProperty("server.port", String.valueOf(DEFAULT_PORT)));

        // Запускаем WebSocket сервер
        TrafficLightServer server = new TrafficLightServer(port);
        server.start();
        System.out.println("WebSocket server started on port " + port);

        // Запускаем JIRA интеграцию (если настроена)
        startJiraIntegration(config, server);

        // Держим процесс живым
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            try {
                server.stop(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        System.out.println("Server is running. Press Ctrl+C to stop.");
    }

    private static Properties loadConfig() {
        Properties props = new Properties();

        // Пробуем загрузить server.properties
        try (FileInputStream fis = new FileInputStream("server.properties")) {
            props.load(fis);
            System.out.println("Loaded server.properties");
            return props;
        } catch (IOException e) {
            System.out.println("server.properties not found, using defaults");
        }

        // Дефолтные значения
        props.setProperty("server.port", String.valueOf(DEFAULT_PORT));
        return props;
    }

    private static String getConfigValue(Properties props, String propKey, String envKey) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty() && !envValue.startsWith("${")) {
            return envValue;
        }

        String propValue = props.getProperty(propKey);
        if (propValue != null && !propValue.isEmpty() && !propValue.startsWith("${")) {
            return propValue;
        }

        return null;
    }

    private static void startJiraIntegration(Properties config, TrafficLightServer server) {
        // Приоритет: переменные окружения > properties файл
        String jiraUrl = getConfigValue(config, "jira.url", "JIRA_URL");
        String jiraUsername = getConfigValue(config, "jira.username", "JIRA_USERNAME");
        String jiraPassword = getConfigValue(config, "jira.password", "JIRA_PASSWORD");

        if (jiraUrl == null || jiraUrl.isEmpty() ||
            jiraUsername == null || jiraUsername.isEmpty() ||
            jiraPassword == null || jiraPassword.isEmpty()) {
            System.out.println("JIRA settings not found, skipping JIRA integration");
            System.out.println("Configure jira.url, jira.username, jira.password in server.properties to enable");
            return;
        }

        // Проверяем наличие кастомного JQL запроса
        String customJql = config.getProperty("jira.jql");

        // Если нет кастомного JQL, формируем запрос по старым параметрам (обратная совместимость)
        if (customJql == null || customJql.isEmpty() || customJql.startsWith("${")) {
            String issueType = config.getProperty("jira.issue.type", "11206");
            customJql = "issuetype = " + issueType + " AND status NOT IN (Closed,Resolved,Done)";
            System.out.println("Используется автоматически сформированный JQL запрос");
        } else {
            System.out.println("Используется кастомный JQL запрос из конфигурации");
        }

        int pollInterval = Integer.parseInt(config.getProperty("jira.poll.interval", "5"));

        try {
            System.out.println("Starting JIRA integration...");
            System.out.println("JIRA URL: " + jiraUrl);
            System.out.println("JIRA Username: " + jiraUsername);
            System.out.println("JQL Query: " + customJql);
            System.out.println("Poll interval: " + pollInterval + " minutes");

            JiraClient jiraClient = new JiraClient(jiraUrl, jiraUsername, jiraPassword);
            JiraPoller jiraPoller = new JiraPoller(jiraClient, server, customJql, pollInterval);
            jiraPoller.start();

            System.out.println("JIRA integration started successfully");
        } catch (Exception e) {
            System.err.println("Failed to start JIRA integration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
