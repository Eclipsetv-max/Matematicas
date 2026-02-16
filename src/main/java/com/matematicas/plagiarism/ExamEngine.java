package com.matematicas.plagiarism;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Motor principal para creación y evaluación de exámenes.
 */
public final class ExamEngine {
    private final StorageService storageService;
    private final PlagiarismDetector plagiarismDetector;
    private final AIValidationClient aiValidationClient;
    private final AppConfiguration configuration;
    private final Random random;

    public ExamEngine(StorageService storageService,
                      PlagiarismDetector plagiarismDetector,
                      AIValidationClient aiValidationClient,
                      AppConfiguration configuration) {
        this.storageService = storageService;
        this.plagiarismDetector = plagiarismDetector;
        this.aiValidationClient = aiValidationClient;
        this.configuration = configuration;
        this.random = new Random();
    }

    public List<Question> buildExam() {
        List<Question> fullBank = QuestionBank.build();
        List<Question> selected = new ArrayList<>();
        int limit = Math.min(configuration.getQuestionLimit(), fullBank.size());

        List<Question> mutable = new ArrayList<>(fullBank);
        while (selected.size() < limit && !mutable.isEmpty()) {
            int index = random.nextInt(mutable.size());
            selected.add(mutable.remove(index));
        }
        return selected;
    }

    public ExamResult evaluate(String studentName, List<Question> questions, Map<String, String> answers) {
        LocalDateTime now = LocalDateTime.now();
        ExamSubmission submission = new ExamSubmission(studentName, now, new LinkedHashMap<>(answers));

        List<QuestionEvaluation> evaluations = new ArrayList<>();
        int totalScore = 0;
        int maxScore = 0;
        double plagiarismSum = 0;

        for (Question question : questions) {
            String answer = submission.getAnswersByQuestionId().getOrDefault(question.getId(), "");
            int score = score(question, answer);
            double risk = plagiarismDetector.detectRisk(answer, question.getRespuestaEsperada(), question.getKeywords());
            List<String> reasons = plagiarismDetector.explainRisk(answer, question.getRespuestaEsperada(), question.getKeywords());

            AIValidationClient.AIValidation aiValidation = aiValidationClient.validate(question, answer);
            if (aiValidation.enabled() && aiValidation.correct()) {
                score = Math.min(question.getMaxScore(), score + 1);
            }

            String scoreFeedback = buildScoreFeedback(question, answer, score, risk, reasons);

            QuestionEvaluation evaluation = new QuestionEvaluation(
                    question.getId(),
                    question.getTema(),
                    question.getEnunciado(),
                    question.getRespuestaEsperada(),
                    answer,
                    score,
                    question.getMaxScore(),
                    risk,
                    aiValidation.enabled(),
                    aiValidation.feedback(),
                    scoreFeedback
            );
            evaluations.add(evaluation);

            totalScore += score;
            maxScore += question.getMaxScore();
            plagiarismSum += risk;
        }

        double averagePlagiarism = evaluations.isEmpty() ? 0 : plagiarismSum / evaluations.size();
        ExamResult result = new ExamResult(submission.getStudentName(), now, totalScore, maxScore, averagePlagiarism, evaluations);
        storageService.saveResult(result);
        return result;
    }

    private int score(Question question, String answer) {
        if (answer == null || answer.isBlank()) {
            return 0;
        }

        String normalizedAnswer = TextUtils.normalize(answer);
        String normalizedExpected = TextUtils.normalize(question.getRespuestaEsperada());

        int points = 0;

        if (normalizedAnswer.contains(normalizedExpected)) {
            points += Math.max(1, question.getMaxScore() - 1);
        }

        int keywordHits = 0;
        for (String keyword : question.getKeywords()) {
            String normalizedKeyword = TextUtils.normalize(keyword);
            if (normalizedAnswer.contains(normalizedKeyword)) {
                keywordHits++;
            }
        }

        if (!question.getKeywords().isEmpty()) {
            double coverage = (double) keywordHits / question.getKeywords().size();
            points += (int) Math.round(coverage * question.getMaxScore());
        }

        if (answer.length() > 45) {
            points += 1;
        }

        return Math.min(question.getMaxScore(), points);
    }

    private String buildScoreFeedback(Question question, String answer, int score, double risk, List<String> reasons) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tema: ").append(question.getTema()).append("\n");
        sb.append("Puntaje: ").append(score).append("/").append(question.getMaxScore()).append("\n");

        if (answer == null || answer.isBlank()) {
            sb.append("Respuesta vacía.\n");
        } else {
            sb.append("Extensión: ").append(answer.length()).append(" caracteres.\n");
        }

        sb.append("Riesgo de plagio IA: ").append(String.format("%.2f", risk)).append("\n");
        for (String reason : reasons) {
            sb.append("- ").append(reason).append("\n");
        }

        if (score == question.getMaxScore()) {
            sb.append("Excelente respuesta.\n");
        } else if (score >= question.getMaxScore() / 2) {
            sb.append("Respuesta aceptable con margen de mejora.\n");
        } else {
            sb.append("Debes reforzar este tema y justificar mejor tu solución.\n");
        }

        return sb.toString().trim();
    }
}
