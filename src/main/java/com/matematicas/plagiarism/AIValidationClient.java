package com.matematicas.plagiarism;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente para validar respuestas contra un endpoint de IA.
 */
public final class AIValidationClient {
    private final AppConfiguration configuration;
    private final HttpClient httpClient;

    public AIValidationClient(AppConfiguration configuration) {
        this.configuration = configuration;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    public AIValidation validate(Question question, String studentAnswer) {
        if (configuration.getAiEndpoint().isBlank()) {
            return AIValidation.disabled("Integración IA desactivada (sin endpoint)");
        }

        String payload = buildJson(question, studentAnswer);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(configuration.getAiEndpoint()))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload));

        if (!configuration.getAiApiKey().isBlank()) {
            builder.header("Authorization", "Bearer " + configuration.getAiApiKey());
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body() == null ? "" : response.body();

            if (status >= 200 && status < 300) {
                boolean correct = parseCorrectness(body);
                String feedback = parseFeedback(body);
                return AIValidation.enabled(correct, feedback.isBlank() ? "Validación IA exitosa" : feedback);
            }
            return AIValidation.enabled(false, "Endpoint IA devolvió estado " + status);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return AIValidation.enabled(false, "Error de conexión IA: " + e.getMessage());
        }
    }

    private String buildJson(Question question, String answer) {
        return "{" +
                "\"questionId\":\"" + escape(question.getId()) + "\"," +
                "\"question\":\"" + escape(question.getEnunciado()) + "\"," +
                "\"expected\":\"" + escape(question.getRespuestaEsperada()) + "\"," +
                "\"answer\":\"" + escape(answer) + "\"" +
                "}";
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private boolean parseCorrectness(String json) {
        String lower = json.toLowerCase();
        return lower.contains("\"correct\":true") || lower.contains("\"isCorrect\":true");
    }

    private String parseFeedback(String json) {
        int idx = json.indexOf("feedback");
        if (idx < 0) {
            return "";
        }
        int colon = json.indexOf(':', idx);
        if (colon < 0) {
            return "";
        }
        int start = json.indexOf('"', colon + 1);
        int end = json.indexOf('"', start + 1);
        if (start < 0 || end < 0 || end <= start) {
            return "";
        }
        return json.substring(start + 1, end);
    }

    public record AIValidation(boolean enabled, boolean correct, String feedback) {
        public static AIValidation disabled(String feedback) {
            return new AIValidation(false, false, feedback);
        }

        public static AIValidation enabled(boolean correct, String feedback) {
            return new AIValidation(true, correct, feedback);
        }
    }
}
