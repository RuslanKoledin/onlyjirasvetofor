package incuat.kg.svetoofor.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Клиент для работы с JIRA REST API
 */
public class JiraClient {

    private final String jiraUrl;
    private final String username;
    private final String password;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JiraClient(String jiraUrl, String username, String password) {
        // Валидация входных данных
        if (jiraUrl == null || jiraUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("JIRA URL cannot be null or empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Проверка URL формата
        if (!jiraUrl.startsWith("http://") && !jiraUrl.startsWith("https://")) {
            throw new IllegalArgumentException("JIRA URL must start with http:// or https://");
        }

        this.jiraUrl = jiraUrl.endsWith("/") ? jiraUrl : jiraUrl + "/";
        this.username = username;
        this.password = password;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        this.objectMapper = new ObjectMapper();
    }

    /**
     * Поиск инцидентов по JQL запросу
     *
     * @param jql          JQL запрос
     * @param startAt      Начальная позиция
     * @param maxResults   Максимальное количество результатов (-1 для всех)
     * @param fields       Поля для получения
     * @return Результат поиска
     * @throws IOException При ошибке запроса
     */
    public JiraSearchResult searchIssues(String jql, int startAt, int maxResults, String[] fields) throws IOException {
        String url = jiraUrl + "rest/api/2/search";

        // Формируем JSON запрос
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"jql\":\"").append(escapeJson(jql)).append("\",");
        jsonBuilder.append("\"startAt\":").append(startAt).append(",");
        jsonBuilder.append("\"maxResults\":").append(maxResults);

        if (fields != null && fields.length > 0) {
            jsonBuilder.append(",\"fields\":[");
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) jsonBuilder.append(",");
                jsonBuilder.append("\"").append(fields[i]).append("\"");
            }
            jsonBuilder.append("]");
        }

        jsonBuilder.append("}");

        RequestBody body = RequestBody.create(
                jsonBuilder.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", getBasicAuthHeader())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("JIRA API error: " + response.code() + " " + response.message());
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, JiraSearchResult.class);
        }
    }

    /**
     * Поиск недавно обновленных инцидентов (за последние N минут)
     *
     * @param issueType    Тип инцидента (например, 11206)
     * @param minutes      Количество минут назад
     * @return Результат поиска
     * @throws IOException При ошибке запроса
     */
    public JiraSearchResult searchRecentIssues(String issueType, int minutes) throws IOException {
        String jql = "issuetype = " + issueType + " AND updated >= -" + minutes + "m";
        String[] fields = {"id", "author", "summary", "key", "status", "priority", "created", "resolutiondate", "issuetype"};

        return searchIssues(jql, 0, -1, fields);
    }

    /**
     * Поиск по кастомному JQL запросу
     *
     * @param customJql    Кастомный JQL запрос
     * @return Результат поиска
     * @throws IOException При ошибке запроса
     */
    public JiraSearchResult searchByCustomJql(String customJql) throws IOException {
        String[] fields = {"id", "creator", "summary", "key", "status", "priority", "created", "resolutiondate", "issuetype"};
        return searchIssues(customJql, 0, -1, fields);
    }

    /**
     * Получить инцидент по ключу
     *
     * @param issueKey Ключ инцидента (например, ITSMJIRA-123456)
     * @return Информация об инциденте
     * @throws IOException При ошибке запроса
     */
    public JiraIssue getIssue(String issueKey) throws IOException {
        String url = jiraUrl + "rest/api/2/issue/" + issueKey;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", getBasicAuthHeader())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("JIRA API error: " + response.code() + " " + response.message());
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, JiraIssue.class);
        }
    }

    /**
     * Проверка подключения к JIRA
     *
     * @return true если подключение успешно
     */
    public boolean testConnection() {
        try {
            String url = jiraUrl + "rest/api/2/myself";

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", getBasicAuthHeader())
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            System.err.println("Ошибка проверки подключения к JIRA: " + e.getMessage());
            return false;
        }
    }

    private String getBasicAuthHeader() {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
}
