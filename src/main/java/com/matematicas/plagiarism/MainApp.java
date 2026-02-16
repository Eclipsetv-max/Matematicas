package com.matematicas.plagiarism;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación.
 */
public final class MainApp {
    private MainApp() {
    }

    public static void main(String[] args) {
        AppConfiguration configuration = AppConfiguration.load();
        StorageService storageService = new StorageService(configuration);
        PlagiarismDetector plagiarismDetector = new PlagiarismDetector();
        AIValidationClient aiValidationClient = new AIValidationClient(configuration);
        ExamEngine examEngine = new ExamEngine(storageService, plagiarismDetector, aiValidationClient, configuration);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(examEngine, storageService, configuration);
            frame.setVisible(true);
        });
    }
}
