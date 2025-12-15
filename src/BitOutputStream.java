import java.io.IOException;
import java.io.OutputStream;

final class BitOutputStream implements AutoCloseable {
    private final OutputStream out;
    private int currentByte;
    private int numBitsFilled;

    BitOutputStream(OutputStream out) {
        this.out = out;
        this.currentByte = 0;
        this.numBitsFilled = 0;
    }

    void writeBit(int bit) throws IOException {
        if (bit != 0 && bit != 1) throw new IllegalArgumentException("bit must be 0/1");
        currentByte = (currentByte << 1) | bit;
        numBitsFilled++;

        if (numBitsFilled == 8) {
            out.write(currentByte);
            numBitsFilled = 0;
            currentByte = 0;
        }
    }

    void writeBits(String bits) throws IOException {
        for (int i = 0; i < bits.length(); i++) {
            char c = bits.charAt(i);
            writeBit(c == '1' ? 1 : 0);
        }
    }

    void flushToByteBoundary() throws IOException {
        if (numBitsFilled == 0) return;
        // pad remaining bits with 0s
        while (numBitsFilled != 0) writeBit(0);
    }

    @Override
    public void close() throws IOException {
        flushToByteBoundary();
        out.flush();
        out.close();
    }
}
