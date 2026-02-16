package com.matematicas.plagiarism;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Detector heurístico de plagio de IA.
 *
 * El detector usa señales de estilo típicas en textos generados automáticamente.
 */
public final class PlagiarismDetector {
    private static final List<String> IA_PHRASES = List.of(
            "como modelo de lenguaje",
            "en conclusión",
            "es importante destacar",
            "sin embargo",
            "por otro lado",
            "en resumen",
            "a continuación",
            "como inteligencia artificial",
            "de manera general",
            "en términos generales",
            "puedo decir",
            "es fundamental",
            "cabe destacar",
            "en este contexto",
            "desde mi perspectiva"
    );

    public double detectRisk(String studentAnswer, String expectedAnswer, List<String> keywords) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return 0.0;
        }

        String normalized = normalize(studentAnswer);
        double phraseRisk = phraseRisk(normalized);
        double repetitionRisk = repetitionRisk(normalized);
        double expectedSimilarity = similarityRisk(normalized, normalize(expectedAnswer));
        double keywordMismatch = keywordMismatchRisk(normalized, keywords);
        double punctuationRisk = punctuationPatternRisk(studentAnswer);

        double weighted =
                phraseRisk * 0.30 +
                repetitionRisk * 0.20 +
                expectedSimilarity * 0.20 +
                keywordMismatch * 0.20 +
                punctuationRisk * 0.10;

        if (normalized.length() > 700 && weighted < 0.35) {
            weighted += 0.10;
        }

        return clamp(weighted);
    }

    private double phraseRisk(String text) {
        int hits = 0;
        for (String phrase : IA_PHRASES) {
            if (text.contains(phrase)) {
                hits++;
            }
        }
        return clamp(hits / 6.0);
    }

    private double repetitionRisk(String text) {
        String[] tokens = text.split("\\s+");
        if (tokens.length < 4) {
            return 0;
        }

        Map<String, Integer> frequency = new java.util.HashMap<>();
        for (String token : tokens) {
            if (token.length() < 4) {
                continue;
            }
            frequency.merge(token, 1, Integer::sum);
        }

        int repetitiveWords = 0;
        for (Integer amount : frequency.values()) {
            if (amount >= 4) {
                repetitiveWords++;
            }
        }

        return clamp((double) repetitiveWords / Math.max(1, frequency.size()));
    }

    private double similarityRisk(String answer, String expected) {
        if (answer.isBlank() || expected.isBlank()) {
            return 0;
        }
        Set<String> answerTokens = new HashSet<>(List.of(answer.split("\\s+")));
        Set<String> expectedTokens = new HashSet<>(List.of(expected.split("\\s+")));

        answerTokens.removeIf(token -> token.length() <= 2);
        expectedTokens.removeIf(token -> token.length() <= 2);

        if (answerTokens.isEmpty() || expectedTokens.isEmpty()) {
            return 0;
        }

        Set<String> intersection = new HashSet<>(answerTokens);
        intersection.retainAll(expectedTokens);

        double ratio = (double) intersection.size() / (double) expectedTokens.size();
        if (ratio > 0.95) {
            return 1.0;
        }
        if (ratio > 0.85) {
            return 0.8;
        }
        if (ratio > 0.70) {
            return 0.6;
        }
        if (ratio > 0.55) {
            return 0.4;
        }
        return 0.1;
    }

    private double keywordMismatchRisk(String answer, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return 0.2;
        }
        int covered = 0;
        for (String keyword : keywords) {
            if (answer.contains(normalize(keyword))) {
                covered++;
            }
        }
        double coverage = (double) covered / (double) keywords.size();
        return clamp(1.0 - coverage);
    }

    private double punctuationPatternRisk(String answer) {
        if (answer == null || answer.isBlank()) {
            return 0.0;
        }

        long commas = answer.chars().filter(ch -> ch == ',').count();
        long periods = answer.chars().filter(ch -> ch == '.').count();
        long semicolons = answer.chars().filter(ch -> ch == ';').count();
        long colons = answer.chars().filter(ch -> ch == ':').count();

        long punctuation = commas + periods + semicolons + colons;
        int words = answer.trim().split("\\s+").length;

        if (words == 0) {
            return 0;
        }

        double density = (double) punctuation / (double) words;
        if (density > 0.35) {
            return 1.0;
        }
        if (density > 0.25) {
            return 0.7;
        }
        if (density > 0.18) {
            return 0.5;
        }
        if (density > 0.12) {
            return 0.3;
        }
        return 0.1;
    }

    public List<String> explainRisk(String studentAnswer, String expectedAnswer, List<String> keywords) {
        List<String> reasons = new ArrayList<>();
        String normalized = normalize(studentAnswer);
        double risk = detectRisk(studentAnswer, expectedAnswer, keywords);

        if (risk > 0.7) {
            reasons.add("Riesgo global alto de uso de texto no original.");
        } else if (risk > 0.45) {
            reasons.add("Riesgo medio: conviene revisar evidencia adicional.");
        } else {
            reasons.add("Riesgo bajo basado en heurísticas locales.");
        }

        for (String phrase : IA_PHRASES) {
            if (normalized.contains(phrase)) {
                reasons.add("Frase típica de IA detectada: '" + phrase + "'.");
            }
        }

        if (keywordMismatchRisk(normalized, keywords) > 0.70) {
            reasons.add("La respuesta omite keywords clave del tema.");
        }
        if (similarityRisk(normalized, normalize(expectedAnswer)) > 0.8) {
            reasons.add("La respuesta es demasiado cercana a la guía esperada.");
        }
        return reasons;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }

    private double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }
}
