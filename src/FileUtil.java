import java.io.*;
import java.security.MessageDigest;

final class FileUtil {

    static int[] countFrequencies(File file) throws IOException {
        int[] freq = new int[256];

        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            int b;
            while ((b = in.read()) != -1) {
                freq[b & 0xFF]++;
            }
        }
        return freq;
    }

    static long fileLength(File file) {
        return file.length();
    }

    static boolean filesEqual(File a, File b) throws IOException {
        if (a.length() != b.length()) return false;

        try (InputStream in1 = new BufferedInputStream(new FileInputStream(a));
             InputStream in2 = new BufferedInputStream(new FileInputStream(b))) {

            byte[] buf1 = new byte[64 * 1024];
            byte[] buf2 = new byte[64 * 1024];

            while (true) {
                int n1 = in1.read(buf1);
                int n2 = in2.read(buf2);
                if (n1 != n2) return false;
                if (n1 == -1) return true;

                for (int i = 0; i < n1; i++) {
                    if (buf1[i] != buf2[i]) return false;
                }
            }
        }
    }

    static String sha256(File f) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) != -1) md.update(buf, 0, n);
        }
        byte[] dig = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte x : dig) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    private FileUtil() {}
}
