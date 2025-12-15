import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class HuffmanApp extends JFrame {

    private final JTextField inputField = new JTextField();
    private final JTextField compressedField = new JTextField();
    private final JTextField decodedField = new JTextField();

    private final JTextArea logArea = new JTextArea(10, 60);
    private final JTextArea headerArea = new JTextArea(10, 60);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Byte", "Char", "Frequency", "Code"}, 0
    );
    private final JTable codeTable = new JTable(tableModel);

    public HuffmanApp() {
        super("Project #2 - Huffman Coding");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildTopPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        JButton chooseInput = new JButton("Choose Input File");
        chooseInput.addActionListener(e -> chooseFileToField(inputField, false));

        JButton chooseCompressed = new JButton("Choose Compressed Output");
        chooseCompressed.addActionListener(e -> chooseFileToField(compressedField, true));

        JButton chooseDecoded = new JButton("Choose Decoded Output");
        chooseDecoded.addActionListener(e -> chooseFileToField(decodedField, true));

        inputField.setEditable(false);
        compressedField.setEditable(false);
        decodedField.setEditable(false);

        c.gridx = 0; c.gridy = 0; c.weightx = 0; p.add(new JLabel("Input:"), c);
        c.gridx = 1; c.weightx = 1; p.add(inputField, c);
        c.gridx = 2; c.weightx = 0; p.add(chooseInput, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; p.add(new JLabel("Compressed (.huf):"), c);
        c.gridx = 1; c.weightx = 1; p.add(compressedField, c);
        c.gridx = 2; c.weightx = 0; p.add(chooseCompressed, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; p.add(new JLabel("Decoded Output:"), c);
        c.gridx = 1; c.weightx = 1; p.add(decodedField, c);
        c.gridx = 2; c.weightx = 0; p.add(chooseDecoded, c);

        return p;
    }

    private JComponent buildCenterPanel() {
        JTabbedPane tabs = new JTabbedPane();

        JScrollPane tableScroll = new JScrollPane(codeTable);
        tabs.addTab("Encoding Table", tableScroll);

        headerArea.setEditable(false);
        headerArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tabs.addTab("Header", new JScrollPane(headerArea));

        logArea.setEditable(false);
        tabs.addTab("Log", new JScrollPane(logArea));

        return tabs;
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton compressBtn = new JButton("Compress");
        compressBtn.addActionListener(e -> runCompress());

        JButton decompressBtn = new JButton("Decompress");
        decompressBtn.addActionListener(e -> runDecompress());

        p.add(compressBtn);
        p.add(decompressBtn);

        return p;
    }

    private void chooseFileToField(JTextField field, boolean saveDialog) {
        JFileChooser fc = new JFileChooser();
        int res = saveDialog ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            field.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void runCompress() {
        File in = textToFile(inputField);
        File out = textToFile(compressedField);

        if (!validateFilesForCompress(in, out)) return;

        clearUI();
        appendLog("Reading file and counting frequencies...");

        try {
            HuffmanService.CompressionResult r = HuffmanService.compress(in, out);

            appendLog("Created Huffman tree + encoding table.");
            appendLog("Encoded and wrote compressed file: " + out.getAbsolutePath());
            appendLog("Original size:   " + r.originalLen + " bytes");
            appendLog("Compressed size: " + r.compressedLen + " bytes");

            fillCodeTable(r.freq, r.codes);
            headerArea.setText(HuffmanService.buildHeaderDisplay(r.freq, r.codes, r.originalLen));
        } catch (Exception ex) {
            appendLog("ERROR: " + ex.getMessage());
            showError(ex);
        }
    }

    private void runDecompress() {
        File compressed = textToFile(compressedField);
        File decoded = textToFile(decodedField);
        File original = textToFile(inputField);

        if (!validateFilesForDecompress(compressed, decoded)) return;

        clearUI();
        appendLog("Reading compressed file header, rebuilding Huffman tree, decoding...");

        try {
            HuffmanService.DecompressionResult r = HuffmanService.decompress(compressed, decoded);
            HuffmanNode root = HuffmanCodec.buildTree(r.freq);
            String[] codes = HuffmanCodec.buildCodes(root);

            fillCodeTable(r.freq, codes);
            headerArea.setText(HuffmanService.buildHeaderDisplay(r.freq, codes, r.originalLen));

            appendLog("Decoded file written: " + decoded.getAbsolutePath());
            appendLog("Decoded bytes: " + r.decodedLen + " / expected: " + r.originalLen);

            if (original != null && original.exists()) {
                boolean same = FileUtil.filesEqual(original, decoded);
                appendLog("Decoded matches original: " + (same ? "YES ✅" : "NO ❌"));
            } else {
                appendLog("Original file not provided (can't auto-compare).");
            }
        } catch (Exception ex) {
            appendLog("ERROR: " + ex.getMessage());
            showError(ex);
        }
    }

    private void fillCodeTable(int[] freq, String[] codes) {
        tableModel.setRowCount(0);
        for (HuffmanService.CodeRow row : HuffmanService.buildCodeRows(freq, codes)) {
            tableModel.addRow(new Object[]{row.byteValue, row.ch, row.freq, row.code});
        }
    }

    private void clearUI() {
        tableModel.setRowCount(0);
        headerArea.setText("");
        logArea.setText("");
    }

    private void appendLog(String s) {
        logArea.append(s + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private File textToFile(JTextField f) {
        String t = f.getText().trim();
        if (t.isEmpty()) return null;
        return new File(t);
    }

    private boolean validateFilesForCompress(File in, File out) {
        if (in == null || !in.exists() || !in.isFile()) {
            JOptionPane.showMessageDialog(this, "Choose a valid input file.", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (out == null) {
            JOptionPane.showMessageDialog(this, "Choose a compressed output file path.", "Missing Output", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateFilesForDecompress(File compressed, File decoded) {
        if (compressed == null || !compressed.exists() || !compressed.isFile()) {
            JOptionPane.showMessageDialog(this, "Choose a valid compressed file.", "Missing Compressed File", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (decoded == null) {
            JOptionPane.showMessageDialog(this, "Choose a decoded output file path.", "Missing Decoded Output", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HuffmanApp().setVisible(true));
    }
}
