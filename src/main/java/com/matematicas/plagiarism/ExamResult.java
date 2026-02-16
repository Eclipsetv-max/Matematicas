package com.matematicas.plagiarism;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado consolidado del examen.
 */
public final class ExamResult {
    private final String studentName;
    private final LocalDateTime timestamp;
    private final int totalScore;
    private final int maxScore;
    private final double plagiarismAverage;
    private final List<QuestionEvaluation> evaluations;

    public ExamResult(String studentName,
                      LocalDateTime timestamp,
                      int totalScore,
                      int maxScore,
                      double plagiarismAverage,
                      List<QuestionEvaluation> evaluations) {
        this.studentName = studentName;
        this.timestamp = timestamp;
        this.totalScore = totalScore;
        this.maxScore = maxScore;
        this.plagiarismAverage = plagiarismAverage;
        this.evaluations = new ArrayList<>(evaluations);
    }

    public String getStudentName() {
        return studentName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public double getPlagiarismAverage() {
        return plagiarismAverage;
    }

    public List<QuestionEvaluation> getEvaluations() {
        return Collections.unmodifiableList(evaluations);
    }

    public String getDisplayScore() {
        return totalScore + " / " + maxScore;
    }
}
