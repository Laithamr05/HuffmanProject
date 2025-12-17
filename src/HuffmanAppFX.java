import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class HuffmanAppFX extends Application {

    private final TextField inputField = new TextField();
    private final TextField compressedField = new TextField();
    private final TextField decodedField = new TextField();

    private final TextArea headerArea = new TextArea();
    private final TextArea logArea = new TextArea();

    private final ObservableList<CodeRowFX> tableData = FXCollections.observableArrayList();
    private TableView<CodeRowFX> table;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Project #2 - Huffman Coding");

        inputField.setEditable(false);
        // Output fields are editable so users can see/modify auto-filled paths
        compressedField.setEditable(true);
        decodedField.setEditable(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        // Top: file pickers
        GridPane filePane = buildFilePane(stage);

        // Buttons row
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button compressBtn = new Button("Compress");
        Button decompressBtn = new Button("Decompress");
        compressBtn.setOnAction(e -> runCompress(stage));
        decompressBtn.setOnAction(e -> runDecompress(stage));
        btnRow.getChildren().addAll(compressBtn, decompressBtn);

        // Table
        table = buildTable();
        VBox tableBox = new VBox(6, new Label("Encoding Table"), table);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Bottom: header + log (simple split)
        headerArea.setEditable(false);
        logArea.setEditable(false);
        headerArea.setWrapText(false);
        logArea.setWrapText(true);

        VBox headerBox = new VBox(6, new Label("Header"), headerArea);
        VBox logBox = new VBox(6, new Label("Log"), logArea);

        SplitPane bottomSplit = new SplitPane(headerBox, logBox);
        bottomSplit.setDividerPositions(0.5);
        bottomSplit.setPrefHeight(220);

        root.getChildren().addAll(filePane, btnRow, tableBox, bottomSplit);

        Scene scene = new Scene(root, 900, 650);
        stage.setScene(scene);
        stage.show();
    }

    private GridPane buildFilePane(Stage stage) {
        GridPane gp = new GridPane();
        gp.setHgap(8);
        gp.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(140);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(140);

        gp.getColumnConstraints().addAll(col1, col2, col3);

        Button inBtn = new Button("Browse...");
        inBtn.setMaxWidth(Double.MAX_VALUE);
        inBtn.setOnAction(e -> chooseOpenFile(stage, inputField));

        Button compBtn = new Button("Browse...");
        compBtn.setMaxWidth(Double.MAX_VALUE);
        compBtn.setOnAction(e -> chooseSaveFile(stage, compressedField, "Compressed Files", "*.huf"));

        Button decBtn = new Button("Browse...");
        decBtn.setMaxWidth(Double.MAX_VALUE);
        decBtn.setOnAction(e -> chooseSaveFile(stage, decodedField, "All Files", "*.*"));

        gp.add(new Label("Input file:"), 0, 0);
        gp.add(inputField, 1, 0);
        gp.add(inBtn, 2, 0);

        gp.add(new Label("Compressed output (.huf):"), 0, 1);
        gp.add(compressedField, 1, 1);
        gp.add(compBtn, 2, 1);

        gp.add(new Label("Decoded output:"), 0, 2);
        gp.add(decodedField, 1, 2);
        gp.add(decBtn, 2, 2);

        return gp;
    }

    private TableView<CodeRowFX> buildTable() {
        TableView<CodeRowFX> tv = new TableView<>(tableData);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CodeRowFX, String> byteCol = new TableColumn<>("Byte");
        byteCol.setCellValueFactory(d -> d.getValue().byteValue);

        TableColumn<CodeRowFX, String> charCol = new TableColumn<>("Char");
        charCol.setCellValueFactory(d -> d.getValue().ch);

        TableColumn<CodeRowFX, String> freqCol = new TableColumn<>("Frequency");
        freqCol.setCellValueFactory(d -> d.getValue().freq);

        TableColumn<CodeRowFX, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(d -> d.getValue().code);

        tv.getColumns().addAll(byteCol, charCol, freqCol, codeCol);
        return tv;
    }

    private void chooseOpenFile(Stage stage, TextField target) {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(stage);
        if (f != null) {
            target.setText(f.getAbsolutePath());
            autoFillOutputsFromInput(f);
        }
    }

    private void chooseSaveFile(Stage stage, TextField target, String desc, String pattern) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, pattern));
        File f = fc.showSaveDialog(stage);
        if (f != null) target.setText(f.getAbsolutePath());
    }

    private void autoFillOutputsFromInput(File inFile) {
        String parent = inFile.getParent();
        String name = inFile.getName();

        int dot = name.lastIndexOf('.');
        String base = (dot >= 0) ? name.substring(0, dot) : name;
        String ext  = (dot >= 0) ? name.substring(dot) : "";

        File huf = new File(parent, base + ".huf");
        File dec = new File(parent, base + "_decoded" + ext);

        compressedField.setText(huf.getAbsolutePath());
        decodedField.setText(dec.getAbsolutePath());
    }

    private void runCompress(Stage stage) {
        File in = textToFile(inputField);
        File out = textToFile(compressedField);

        if (!validateFilesForCompress(stage, in, out)) return;

        clearUI();
        appendLog("Reading file and counting frequencies...");

        try {
            // --- same logic you had in Swing ---
            HuffmanService.CompressionResult r = HuffmanService.compress(in, out);

            appendLog("Created Huffman tree + encoding table.");
            appendLog("Encoded and wrote compressed file: " + out.getAbsolutePath());
            appendLog("Original size:   " + r.originalLen + " bytes");
            
            int headerSize = HuffmanService.calculateHeaderSize(r.freq);
            long compressedDataSize = r.compressedLen - headerSize;
            double compressionRatio = (double) r.compressedLen / r.originalLen;
            
            appendLog("Compressed size: " + r.compressedLen + " bytes");
            appendLog("  - Header:      " + headerSize + " bytes");
            appendLog("  - Data:        " + compressedDataSize + " bytes");
            appendLog("Compression ratio: " + String.format("%.2f", compressionRatio) + "x");
            
            if (r.compressedLen > r.originalLen) {
                appendLog("⚠️  Note: File expanded due to header overhead (common for small files)");
            }

            fillCodeTable(r.freq, r.codes);
            headerArea.setText(HuffmanService.buildHeaderDisplay(r.freq, r.codes, r.originalLen));
        } catch (Exception ex) {
            appendLog("ERROR: " + ex.getMessage());
            showError(stage, ex);
        }
    }

    private void runDecompress(Stage stage) {
        File compressed = textToFile(compressedField);
        File decoded = textToFile(decodedField);
        File original = textToFile(inputField);

        if (!validateFilesForDecompress(stage, compressed, decoded)) return;

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
            showError(stage, ex);
        }
    }

    private void fillCodeTable(int[] freq, String[] codes) {
        tableData.clear();
        for (HuffmanService.CodeRow row : HuffmanService.buildCodeRows(freq, codes)) {
            tableData.add(new CodeRowFX(
                    String.valueOf(row.byteValue),
                    row.ch,
                    String.valueOf(row.freq),
                    row.code
            ));
        }
    }

    private void clearUI() {
        tableData.clear();
        headerArea.clear();
        logArea.clear();
    }

    private void appendLog(String s) {
        logArea.appendText(s + "\n");
    }

    private void showError(Stage stage, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(stage);
        a.setTitle("Error");
        a.setHeaderText("An error occurred");
        a.setContentText(ex.toString());
        a.showAndWait();
    }

    private File textToFile(TextField f) {
        String t = f.getText().trim();
        if (t.isEmpty()) return null;
        return new File(t);
    }

    private boolean validateFilesForCompress(Stage stage, File in, File out) {
        if (in == null || !in.exists() || !in.isFile()) {
            showWarn(stage, "Choose a valid input file.");
            return false;
        }
        if (out == null) {
            showWarn(stage, "Choose a compressed output file path.");
            return false;
        }
        return true;
    }

    private boolean validateFilesForDecompress(Stage stage, File compressed, File decoded) {
        if (compressed == null || !compressed.exists() || !compressed.isFile()) {
            showWarn(stage, "Choose a valid compressed file.");
            return false;
        }
        if (decoded == null) {
            showWarn(stage, "Choose a decoded output file path.");
            return false;
        }
        return true;
    }

    private void showWarn(Stage stage, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.initOwner(stage);
        a.setTitle("Missing Data");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // Simple TableView row model
    public static class CodeRowFX {
        final SimpleStringProperty byteValue = new SimpleStringProperty();
        final SimpleStringProperty ch = new SimpleStringProperty();
        final SimpleStringProperty freq = new SimpleStringProperty();
        final SimpleStringProperty code = new SimpleStringProperty();

        public CodeRowFX(String b, String c, String f, String code) {
            this.byteValue.set(b);
            this.ch.set(c);
            this.freq.set(f);
            this.code.set(code);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
