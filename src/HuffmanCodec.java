final class HuffmanCodec {

    /** Build Huffman tree using the exact priority-queue loop shown in the slides. fileciteturn3file1L77-L89 */
    static HuffmanNode buildTree(int[] freq) {
        MinPriorityQueue q = new MinPriorityQueue(256);

        int leaves = 0;
        for (int b = 0; b < 256; b++) {
            if (freq[b] > 0) {
                q.insert(new HuffmanNode(b, freq[b]));
                leaves++;
            }
        }

        if (leaves == 0) return null;

        // Special case: only 1 unique byte -> make a root with a single child
        if (leaves == 1) {
            HuffmanNode only = q.deleteMin();
            return new HuffmanNode(only, null);
        }

        for (int i = 1; i < leaves; i++) {
            HuffmanNode x = q.deleteMin();
            HuffmanNode y = q.deleteMin();
            HuffmanNode z = new HuffmanNode(x, y);
            q.insert(z);
        }
        return q.deleteMin();
    }

    static String[] buildCodes(HuffmanNode root) {
        String[] codes = new String[256];
        if (root == null) return codes;
        dfs(root, "", codes);
        return codes;
    }

    private static void dfs(HuffmanNode node, String path, String[] codes) {
        if (node == null) return;

        if (node.isLeaf()) {
            // If tree has 1 unique byte, path could be "", force "0"
            codes[node.byteValue] = path.isEmpty() ? "0" : path;
            return;
        }

        // Left child -> 0, Right child -> 1. fileciteturn3file2L15-L18
        dfs(node.left, path + "0", codes);
        dfs(node.right, path + "1", codes);
    }
}
