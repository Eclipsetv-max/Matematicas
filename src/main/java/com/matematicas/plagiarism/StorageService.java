package com.matematicas.plagiarism;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia local de resultados.
 */
public final class StorageService {
    private static final String SEP = "|";
    private final AppConfiguration configuration;

    public StorageService(AppConfiguration configuration) {
        this.configuration = configuration;
        initializeFile();
    }

    private void initializeFile() {
        Path file = configuration.getResultsFile();
        if (!Files.exists(file)) {
            String header = "timestamp|student|total|max|avgPlagiarism|questionId|topic|score|maxScore|risk|aiValidated|aiFeedback|question|expected|answer|feedback\n";
            try {
                Files.writeString(file, header, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo inicializar archivo de resultados", e);
            }
        }
    }

    public synchronized void saveResult(ExamResult result) {
        List<String> lines = new ArrayList<>();
        for (QuestionEvaluation ev : result.getEvaluations()) {
            String line = sanitize(result.getTimestamp().toString()) + SEP +
                    sanitize(result.getStudentName()) + SEP +
                    result.getTotalScore() + SEP +
                    result.getMaxScore() + SEP +
                    String.format("%.4f", result.getPlagiarismAverage()) + SEP +
                    sanitize(ev.questionId()) + SEP +
                    sanitize(ev.topic()) + SEP +
                    ev.score() + SEP +
                    ev.maxScore() + SEP +
                    String.format("%.4f", ev.plagiarismRisk()) + SEP +
                    ev.aiValidated() + SEP +
                    sanitize(ev.aiFeedback()) + SEP +
                    sanitize(ev.prompt()) + SEP +
                    sanitize(ev.expectedAnswer()) + SEP +
                    sanitize(ev.studentAnswer()) + SEP +
                    sanitize(ev.scoringFeedback());
            lines.add(line);
        }

        try {
            Files.write(configuration.getResultsFile(), lines, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar resultado", e);
        }
    }

    public synchronized List<StoredResultRecord> loadAllRecords() {
        Path file = configuration.getResultsFile();
        List<StoredResultRecord> records = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length < 16) {
                    continue;
                }
                records.add(new StoredResultRecord(
                        parseDate(parts[0]),
                        unsanitize(parts[1]),
                        parseInt(parts[2]),
                        parseInt(parts[3]),
                        parseDouble(parts[4]),
                        unsanitize(parts[5]),
                        unsanitize(parts[6]),
                        parseInt(parts[7]),
                        parseInt(parts[8]),
                        parseDouble(parts[9]),
                        Boolean.parseBoolean(parts[10]),
                        unsanitize(parts[11]),
                        unsanitize(parts[12]),
                        unsanitize(parts[13]),
                        unsanitize(parts[14]),
                        unsanitize(parts[15])
                ));
            }
        } catch (IOException e) {
            return records;
        }
        return records;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private LocalDateTime parseDate(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("|", "\\p")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String unsanitize(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\n", "\n")
                .replace("\\p", "|")
                .replace("\\\\", "\\");
    }

    public record StoredResultRecord(
            LocalDateTime timestamp,
            String student,
            int total,
            int max,
            double avgPlagiarism,
            String questionId,
            String topic,
            int score,
            int maxScore,
            double risk,
            boolean aiValidated,
            String aiFeedback,
            String question,
            String expected,
            String answer,
            String feedback
    ) {
    }
}
