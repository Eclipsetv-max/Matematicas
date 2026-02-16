package com.matematicas.plagiarism;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

/**
 * Configuración de la aplicación.
 */
public final class AppConfiguration {
    private static final String CONFIG_FILE_NAME = "app.properties";
    private static final String DEFAULT_AI_ENDPOINT = "";
    private static final String DEFAULT_AI_API_KEY = "";

    private final Path dataDirectory;
    private final String aiEndpoint;
    private final String aiApiKey;
    private final int questionLimit;

    private AppConfiguration(Path dataDirectory, String aiEndpoint, String aiApiKey, int questionLimit) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.aiEndpoint = aiEndpoint == null ? "" : aiEndpoint;
        this.aiApiKey = aiApiKey == null ? "" : aiApiKey;
        this.questionLimit = Math.max(5, questionLimit);
    }

    public static AppConfiguration load() {
        Properties properties = new Properties();
        Path localFile = Paths.get(CONFIG_FILE_NAME);
        if (Files.exists(localFile)) {
            try (InputStream inputStream = Files.newInputStream(localFile)) {
                properties.load(inputStream);
            } catch (IOException ignored) {
                // Si falla, usamos defaults.
            }
        }

        String dataDirValue = getOrDefault(properties, "data.dir", "data");
        String endpoint = getOrDefault(properties, "ai.endpoint", DEFAULT_AI_ENDPOINT);
        String apiKey = getOrDefault(properties, "ai.apiKey", DEFAULT_AI_API_KEY);
        int limit = parseInt(getOrDefault(properties, "exam.questionLimit", "10"), 10);

        Path dataDir = Paths.get(dataDirValue);
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible crear el directorio de datos: " + dataDir, e);
        }

        return new AppConfiguration(dataDir, endpoint, apiKey, limit);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String getOrDefault(Properties props, String key, String defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Path getResultsFile() {
        return dataDirectory.resolve("exam_results.csv");
    }

    public Path getSessionCacheFile() {
        return dataDirectory.resolve("session_cache.json");
    }

    public String getAiEndpoint() {
        return aiEndpoint;
    }

    public String getAiApiKey() {
        return aiApiKey;
    }

    public int getQuestionLimit() {
        return questionLimit;
    }
}
