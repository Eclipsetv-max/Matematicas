package com.matematicas.plagiarism;

/**
 * Evaluación por pregunta.
 */
public record QuestionEvaluation(
        String questionId,
        String topic,
        String prompt,
        String expectedAnswer,
        String studentAnswer,
        int score,
        int maxScore,
        double plagiarismRisk,
        boolean aiValidated,
        String aiFeedback,
        String scoringFeedback
) {
}
