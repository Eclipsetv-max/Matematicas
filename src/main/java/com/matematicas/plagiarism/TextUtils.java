package com.matematicas.plagiarism;

import java.text.Normalizer;
import java.util.Locale;

public final class TextUtils {
    private TextUtils() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String safe(String text) {
        return text == null ? "" : text.trim();
    }
}
