final class HuffmanNode implements Comparable<HuffmanNode> {
    final int freq;
    final int byteValue; // 0..255 for leaf; -1 for internal
    final HuffmanNode left;
    final HuffmanNode right;

    HuffmanNode(int byteValue, int freq) {
        this.byteValue = byteValue;
        this.freq = freq;
        this.left = null;
        this.right = null;
    }

    HuffmanNode(HuffmanNode left, HuffmanNode right) {
        this.byteValue = -1;
        this.left = left;
        this.right = right;
        int lf = (left == null) ? 0 : left.freq;
        int rf = (right == null) ? 0 : right.freq;
        this.freq = lf + rf;
    }

    boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        // smaller frequency = higher priority
        int c = Integer.compare(this.freq, o.freq);
        if (c != 0) return c;
        // tie-breaker: leaf nodes first, then by byte value for stable output
        if (this.isLeaf() != o.isLeaf()) return this.isLeaf() ? -1 : 1;
        return Integer.compare(this.byteValue, o.byteValue);
    }
}
