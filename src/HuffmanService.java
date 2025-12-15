import java.io.*;
import java.util.ArrayList;
import java.util.List;

final class HuffmanService {

    static final String MAGIC = "HUF1";

    static CompressionResult compress(File input, File output) throws Exception {
        int[] freq = FileUtil.countFrequencies(input); // Read file and count frequencies. fileciteturn3file0L20-L24
        HuffmanNode root = HuffmanCodec.buildTree(freq); // Create Huffman coding tree. fileciteturn3file0L21-L22
        String[] codes = HuffmanCodec.buildCodes(root);  // Create table of encodings. fileciteturn3file0L22-L24

        long originalLen = FileUtil.fileLength(input);

        try (InputStream in = new BufferedInputStream(new FileInputStream(input));
             OutputStream fos = new BufferedOutputStream(new FileOutputStream(output));
             DataOutputStream headerOut = new DataOutputStream(fos)) {

            // Header must include the Huffman code info (we store frequencies -> reconstruct tree/codes). fileciteturn3file0L25-L27
            headerOut.writeBytes(MAGIC);
            headerOut.writeLong(originalLen);

            for (int i = 0; i < 256; i++) headerOut.writeInt(freq[i]);

            // Now write compressed bits
            try (BitOutputStream bitOut = new BitOutputStream(fos)) {
                int b;
                while ((b = in.read()) != -1) {
                    String code = codes[b & 0xFF];
                    if (code == null) throw new IllegalStateException("Missing code for byte " + (b & 0xFF));
                    bitOut.writeBits(code);
                }
            }
        }

        return new CompressionResult(freq, codes, originalLen, output.length());
    }

    static DecompressionResult decompress(File compressed, File outputDecoded) throws Exception {
        int[] freq = new int[256];
        long originalLen;

        try (InputStream fis = new BufferedInputStream(new FileInputStream(compressed));
             DataInputStream headerIn = new DataInputStream(fis)) {

            byte[] magicBytes = new byte[4];
            headerIn.readFully(magicBytes);
            String magic = new String(magicBytes);
            if (!MAGIC.equals(magic)) throw new IOException("Not a Huffman file (bad magic): " + magic);

            originalLen = headerIn.readLong();
            for (int i = 0; i < 256; i++) freq[i] = headerIn.readInt();

            HuffmanNode root = HuffmanCodec.buildTree(freq);
            if (root == null) {
                // Empty original file
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputDecoded))) {
                    // write nothing
                }
                return new DecompressionResult(freq, originalLen, 0);
            }

            try (BitInputStream bitIn = new BitInputStream(fis);
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(outputDecoded))) {

                long written = 0;
                HuffmanNode cur = root;

                while (written < originalLen) {
                    int bit = bitIn.readBit();
                    if (bit == -1) throw new EOFException("Unexpected end of compressed bit stream");
                    cur = (bit == 0) ? cur.left : cur.right;
                    if (cur == null) throw new IOException("Corrupted stream: reached null node");

                    if (cur.isLeaf()) {
                        out.write(cur.byteValue);
                        written++;
                        cur = root;
                    }
                }
                out.flush();
                return new DecompressionResult(freq, originalLen, written);
            }
        }
    }

    static String buildHeaderDisplay(int[] freq, String[] codes, long originalLen) {
        StringBuilder sb = new StringBuilder();
        sb.append("Header (HUF1)\n");
        sb.append("Original Length (bytes): ").append(originalLen).append("\n");
        sb.append("Non-zero frequencies + codes:\n");

        for (int b = 0; b < 256; b++) {
            if (freq[b] > 0) {
                sb.append(String.format("byte=%3d  char=%s  freq=%d  code=%s%n",
                        b, printable(b), freq[b], (codes == null ? "-" : codes[b])));
            }
        }
        return sb.toString();
    }

    static List<CodeRow> buildCodeRows(int[] freq, String[] codes) {
        List<CodeRow> rows = new ArrayList<>();
        for (int b = 0; b < 256; b++) {
            if (freq[b] > 0) {
                rows.add(new CodeRow(b, printable(b), freq[b], codes[b]));
            }
        }
        return rows;
    }

    private static String printable(int b) {
        if (b >= 32 && b <= 126) return "'" + (char) b + "'";
        if (b == 10) return "'\\n'";
        if (b == 13) return "'\\r'";
        if (b == 9) return "'\\t'";
        return "(non-printable)";
    }

    static final class CodeRow {
        final int byteValue;
        final String ch;
        final int freq;
        final String code;

        CodeRow(int byteValue, String ch, int freq, String code) {
            this.byteValue = byteValue;
            this.ch = ch;
            this.freq = freq;
            this.code = code;
        }
    }

    static final class CompressionResult {
        final int[] freq;
        final String[] codes;
        final long originalLen;
        final long compressedLen;

        CompressionResult(int[] freq, String[] codes, long originalLen, long compressedLen) {
            this.freq = freq;
            this.codes = codes;
            this.originalLen = originalLen;
            this.compressedLen = compressedLen;
        }
    }

    static final class DecompressionResult {
        final int[] freq;
        final long originalLen;
        final long decodedLen;

        DecompressionResult(int[] freq, long originalLen, long decodedLen) {
            this.freq = freq;
            this.originalLen = originalLen;
            this.decodedLen = decodedLen;
        }
    }

    private HuffmanService() {}
}
