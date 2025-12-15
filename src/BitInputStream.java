import java.io.IOException;
import java.io.InputStream;

final class BitInputStream implements AutoCloseable {
    private final InputStream in;
    private int currentByte;
    private int numBitsRemaining;

    BitInputStream(InputStream in) {
        this.in = in;
        this.currentByte = 0;
        this.numBitsRemaining = 0;
    }

    /** @return next bit (0/1) or -1 if end of stream */
    int readBit() throws IOException {
        if (numBitsRemaining == 0) {
            currentByte = in.read();
            if (currentByte == -1) return -1;
            numBitsRemaining = 8;
        }
        numBitsRemaining--;
        return (currentByte >>> numBitsRemaining) & 1;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
