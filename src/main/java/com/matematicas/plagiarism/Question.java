package com.matematicas.plagiarism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pregunta de examen.
 */
public final class Question {
    private final String id;
    private final String enunciado;
    private final String respuestaEsperada;
    private final String tema;
    private final int maxScore;
    private final List<String> keywords;

    public Question(String id, String enunciado, String respuestaEsperada, String tema, int maxScore, List<String> keywords) {
        this.id = Objects.requireNonNull(id, "id");
        this.enunciado = Objects.requireNonNull(enunciado, "enunciado");
        this.respuestaEsperada = Objects.requireNonNull(respuestaEsperada, "respuestaEsperada");
        this.tema = Objects.requireNonNull(tema, "tema");
        this.maxScore = Math.max(1, maxScore);
        this.keywords = new ArrayList<>(Objects.requireNonNullElseGet(keywords, ArrayList::new));
    }

    public String getId() {
        return id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getRespuestaEsperada() {
        return respuestaEsperada;
    }

    public String getTema() {
        return tema;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public List<String> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }
}
