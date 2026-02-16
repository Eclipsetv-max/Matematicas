package com.matematicas.plagiarism;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Envío de examen por usuario.
 */
public final class ExamSubmission {
    private final String studentName;
    private final LocalDateTime submittedAt;
    private final Map<String, String> answersByQuestionId;

    public ExamSubmission(String studentName, LocalDateTime submittedAt, Map<String, String> answersByQuestionId) {
        this.studentName = sanitize(studentName);
        this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
        this.answersByQuestionId = new LinkedHashMap<>(Objects.requireNonNull(answersByQuestionId, "answersByQuestionId"));
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "Sin Nombre";
        }
        return value.trim();
    }

    public String getStudentName() {
        return studentName;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Map<String, String> getAnswersByQuestionId() {
        return answersByQuestionId;
    }
}
