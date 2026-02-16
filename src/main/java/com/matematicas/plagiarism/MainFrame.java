package com.matematicas.plagiarism;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interfaz Swing principal con decoración verde.
 */
public final class MainFrame extends JFrame {
    private static final Color MAIN_GREEN = new Color(18, 110, 60);
    private static final Color SOFT_GREEN = new Color(227, 248, 232);
    private static final Color STRONG_GREEN = new Color(9, 85, 45);

    private final ExamEngine examEngine;
    private final StorageService storageService;
    private final AppConfiguration configuration;

    private JTextField studentField;
    private JPanel questionPanel;
    private JTextArea outputArea;
    private javax.swing.JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JTextArea historyDetailArea;
    private List<Question> currentQuestions;
    private final Map<String, JTextArea> answerFields;

    public MainFrame(ExamEngine examEngine, StorageService storageService, AppConfiguration configuration) {
        this.examEngine = examEngine;
        this.storageService = storageService;
        this.configuration = configuration;
        this.answerFields = new LinkedHashMap<>();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback natural.
        }

        setTitle("Sistema de Examen + Detector de Plagio IA");
        setSize(1280, 860);
        setMinimumSize(new Dimension(980, 680));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Examen", buildExamTab());
        tabs.addTab("Historial", buildHistoryTab());
        add(tabs, BorderLayout.CENTER);

        refreshExam();
        refreshHistory();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAIN_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Evaluador Matemático + Control de Respuestas IA");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));

        JLabel subtitle = new JLabel("Decoración verde, guardado de puntajes y revisión de respuestas");
        subtitle.setForeground(new Color(220, 255, 220));
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JPanel labels = new JPanel();
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.setBackground(MAIN_GREEN);
        labels.add(title);
        labels.add(Box.createVerticalStrut(6));
        labels.add(subtitle);

        header.add(labels, BorderLayout.WEST);
        return header;
    }

    private JPanel buildExamTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SOFT_GREEN);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(SOFT_GREEN);
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JLabel nameLabel = new JLabel("Nombre del estudiante:");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        studentField = new JTextField(24);
        studentField.setToolTipText("Escribe tu nombre aquí");

        JButton refreshButton = createGreenButton("Nuevo examen", STRONG_GREEN);
        refreshButton.addActionListener(e -> refreshExam());

        JButton submitButton = createGreenButton("Calificar y guardar", MAIN_GREEN);
        submitButton.addActionListener(e -> submitExam());

        topBar.add(nameLabel);
        topBar.add(studentField);
        topBar.add(refreshButton);
        topBar.add(submitButton);

        questionPanel = new JPanel();
        questionPanel.setBackground(SOFT_GREEN);
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));

        JScrollPane questionScroll = new JScrollPane(questionPanel);
        questionScroll.getVerticalScrollBar().setUnitIncrement(14);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBorder(BorderFactory.createTitledBorder("Resultado"));
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, questionScroll, new JScrollPane(outputArea));
        splitPane.setResizeWeight(0.72);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SOFT_GREEN);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        historyTableModel = new DefaultTableModel(new Object[]{"Fecha", "Nombre", "Puntaje", "Riesgo IA", "Pregunta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new javax.swing.JTable(historyTableModel);
        historyTable.setFillsViewportHeight(true);
        historyTable.setRowHeight(24);
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedHistoryDetail();
            }
        });

        historyDetailArea = new JTextArea();
        historyDetailArea.setEditable(false);
        historyDetailArea.setLineWrap(true);
        historyDetailArea.setWrapStyleWord(true);
        historyDetailArea.setBorder(BorderFactory.createTitledBorder("Detalle de respuesta y corrección"));

        JButton reload = createGreenButton("Recargar historial", MAIN_GREEN);
        reload.addActionListener(e -> refreshHistory());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(SOFT_GREEN);
        top.add(reload);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(historyTable),
                new JScrollPane(historyDetailArea));
        split.setResizeWeight(0.52);

        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JButton createGreenButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        return button;
    }

    private void refreshExam() {
        currentQuestions = examEngine.buildExam();
        answerFields.clear();
        questionPanel.removeAll();

        JLabel intro = new JLabel("Responde con claridad. El sistema detectará señales de plagio y validará con IA externa si está configurada.");
        intro.setForeground(STRONG_GREEN);
        intro.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        intro.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));
        questionPanel.add(intro);

        int index = 1;
        for (Question question : currentQuestions) {
            JPanel questionCard = createQuestionCard(index, question);
            questionPanel.add(questionCard);
            questionPanel.add(Box.createVerticalStrut(8));
            index++;
        }

        questionPanel.revalidate();
        questionPanel.repaint();
        outputArea.setText("Examen generado con " + currentQuestions.size() + " preguntas.\n" +
                "Límite configurado: " + configuration.getQuestionLimit() + " preguntas.");
    }

    private JPanel createQuestionCard(int index, Question question) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(160, 210, 170), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String header = index + ". [" + question.getTema() + "]  (" + question.getMaxScore() + " puntos)";
        JLabel title = new JLabel(header);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        title.setForeground(MAIN_GREEN);

        JEditorPane promptArea = new JEditorPane();
        promptArea.setContentType("text/plain");
        promptArea.setText(question.getEnunciado());
        promptArea.setEditable(false);
        promptArea.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
        promptArea.setBackground(Color.WHITE);

        JTextArea answer = new JTextArea(4, 50);
        answer.setLineWrap(true);
        answer.setWrapStyleWord(true);
        answer.setBorder(BorderFactory.createLineBorder(new Color(180, 220, 180), 1));
        answer.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.add(new JLabel("Tu respuesta:"), BorderLayout.NORTH);
        bottom.add(new JScrollPane(answer), BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(promptArea, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        answerFields.put(question.getId(), answer);
        return card;
    }

    private void submitExam() {
        String name = studentField.getText() == null ? "" : studentField.getText().trim();
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debes escribir un nombre en el campo correspondiente.", "Nombre requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, String> answers = new LinkedHashMap<>();
        for (Map.Entry<String, JTextArea> entry : answerFields.entrySet()) {
            answers.put(entry.getKey(), entry.getValue().getText());
        }

        ExamResult result = examEngine.evaluate(name, currentQuestions, answers);
        outputArea.setText(formatResult(result));
        refreshHistory();
    }

    private String formatResult(ExamResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Estudiante: ").append(result.getStudentName()).append("\n");
        sb.append("Fecha: ").append(result.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Puntaje final: ").append(result.getDisplayScore()).append("\n");
        sb.append("Riesgo promedio plagio IA: ").append(String.format("%.2f", result.getPlagiarismAverage())).append("\n");
        sb.append("------------------------------------------------------------\n");

        for (QuestionEvaluation ev : result.getEvaluations()) {
            sb.append("Pregunta ").append(ev.questionId()).append(" [").append(ev.topic()).append("]\n");
            sb.append("Puntaje: ").append(ev.score()).append("/").append(ev.maxScore()).append("\n");
            sb.append("Riesgo IA: ").append(String.format("%.2f", ev.plagiarismRisk())).append("\n");
            sb.append("Validación IA externa: ").append(ev.aiValidated() ? "activa" : "no configurada").append("\n");
            sb.append("Feedback IA: ").append(ev.aiFeedback()).append("\n");
            sb.append("Feedback sistema: ").append(ev.scoringFeedback()).append("\n");
            sb.append("------------------------------------------------------------\n");
        }

        sb.append("Resultado guardado en: ").append(configuration.getResultsFile()).append("\n");
        return sb.toString();
    }

    private void refreshHistory() {
        List<StorageService.StoredResultRecord> records = storageService.loadAllRecords();
        historyTableModel.setRowCount(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (StorageService.StoredResultRecord record : records) {
            String score = record.total() + "/" + record.max();
            historyTableModel.addRow(new Object[]{
                    record.timestamp().format(formatter),
                    record.student(),
                    score,
                    String.format("%.2f", record.avgPlagiarism()),
                    record.questionId()
            });
        }

        historyDetailArea.setText(records.isEmpty()
                ? "Sin resultados guardados todavía."
                : "Selecciona una fila para ver detalle completo.");
    }

    private void showSelectedHistoryDetail() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            return;
        }

        List<StorageService.StoredResultRecord> records = storageService.loadAllRecords();
        if (row >= records.size()) {
            return;
        }

        StorageService.StoredResultRecord record = records.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Fecha: ").append(record.timestamp()).append("\n");
        sb.append("Alumno: ").append(record.student()).append("\n");
        sb.append("Puntaje global: ").append(record.total()).append("/").append(record.max()).append("\n");
        sb.append("Promedio riesgo IA: ").append(String.format("%.2f", record.avgPlagiarism())).append("\n");
        sb.append("Pregunta ID: ").append(record.questionId()).append("\n");
        sb.append("Tema: ").append(record.topic()).append("\n\n");
        sb.append("Enunciado:\n").append(record.question()).append("\n\n");
        sb.append("Respuesta esperada:\n").append(record.expected()).append("\n\n");
        sb.append("Respuesta del estudiante:\n").append(record.answer()).append("\n\n");
        sb.append("Feedback IA:\n").append(record.aiFeedback()).append("\n\n");
        sb.append("Feedback de corrección:\n").append(record.feedback()).append("\n");

        historyDetailArea.setText(sb.toString());
        historyDetailArea.setCaretPosition(0);
    }
}
