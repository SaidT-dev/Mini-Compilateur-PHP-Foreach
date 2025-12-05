import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Interface extends JFrame {
    private final JTextArea codeInput;
    private final JTextArea console;

    public Interface() {
        setTitle("Mini-Compilateur");
        setSize(800, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        codeInput = new JTextArea();
        JScrollPane codeScrollPane = new JScrollPane(codeInput);
        codeScrollPane.setRowHeaderView(new TextLineNumber(codeInput));
        codeScrollPane.setBorder(BorderFactory.createTitledBorder("Editeur"));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 5));
        JButton runButton = new JButton("Executer");
        runButton.addActionListener(e -> execute());
        JButton clearButton = new JButton("Effacer");

        buttons.add(runButton);
        buttons.add(clearButton);
        buttonPanel.add(buttons, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codeScrollPane, buttonPanel);
        splitPane.setResizeWeight(0.8);
        splitPane.setDividerLocation(600);

        contentPane.add(splitPane, BorderLayout.CENTER);

        console = new JTextArea();
        console.setEditable(false);
        console.setLineWrap(true);
        console.setWrapStyleWord(true);
        clearButton.addActionListener(e -> {
            codeInput.setText("");
            console.setText("");
        });
        JScrollPane consoleScrollPane = new JScrollPane(console);
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
        consoleScrollPane.setPreferredSize(new Dimension(800, 150));
        contentPane.add(consoleScrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void execute() {
        String code = codeInput.getText();
        AnalyseurLexical lexer = new AnalyseurLexical(code);
        java.util.List<Token> tokens = lexer.scan();
        try {
            AnalyseurSyntaxique.init(tokens);
            AnalyseurSyntaxique.Z();
            console.setText("Code terminer avec succes !");
        } catch (Exception e) {
            console.setText(e.getMessage());
        }
    }
}
