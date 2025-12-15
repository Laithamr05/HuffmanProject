/**
 * Custom min-priority-queue (binary heap) for HuffmanNode.
 * Operations needed by the slides: insert() and deleteMin(). fileciteturn3file1L77-L89
 */
final class MinPriorityQueue {
    private HuffmanNode[] heap;
    private int size;

    MinPriorityQueue(int capacity) {
        heap = new HuffmanNode[Math.max(4, capacity)];
        size = 0;
    }

    int size() {
        return size;
    }

    boolean isEmpty() {
        return size == 0;
    }

    void insert(HuffmanNode x) {
        if (x == null) throw new IllegalArgumentException("null node");
        ensureCapacity(size + 1);
        heap[size] = x;
        swim(size);
        size++;
    }

    HuffmanNode deleteMin() {
        if (size == 0) throw new IllegalStateException("empty queue");
        HuffmanNode min = heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) sink(0);
        return min;
    }

    private void ensureCapacity(int needed) {
        if (needed <= heap.length) return;
        HuffmanNode[] newHeap = new HuffmanNode[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, heap.length);
        heap = newHeap;
    }

    private void swim(int k) {
        while (k > 0) {
            int parent = (k - 1) / 2;
            if (heap[k].compareTo(heap[parent]) >= 0) break;
            swap(k, parent);
            k = parent;
        }
    }

    private void sink(int k) {
        while (true) {
            int left = 2 * k + 1;
            int right = 2 * k + 2;
            if (left >= size) break;

            int smallest = left;
            if (right < size && heap[right].compareTo(heap[left]) < 0) {
                smallest = right;
            }
            if (heap[k].compareTo(heap[smallest]) <= 0) break;

            swap(k, smallest);
            k = smallest;
        }
    }

    private void swap(int i, int j) {
        HuffmanNode tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }
}
