# Huffman Coding Project

A complete Java implementation of Huffman coding compression and decompression with a modern JavaFX graphical user interface. This project demonstrates lossless data compression using variable-length prefix codes based on character frequencies, achieving optimal compression ratios for text and binary files.

## Overview

Huffman coding is a lossless data compression algorithm that assigns variable-length binary codes to characters based on their frequency of occurrence. More frequent characters receive shorter codes, resulting in efficient compression. This implementation provides a full-featured compression tool with both command-line capabilities and an intuitive GUI.

## Features

- **Complete Huffman Algorithm Implementation**: Builds optimal prefix codes using frequency analysis
- **Graphical User Interface**: Modern JavaFX-based GUI for easy file compression and decompression
- **Custom Bit I/O Streams**: Efficient bit-level reading and writing for compressed data
- **Optimized Header Format**: Stores only non-zero frequencies to minimize header overhead
- **Encoding Table Visualization**: Displays character frequencies and their Huffman codes
- **Compression Statistics**: Shows original size, compressed size, header size, and compression ratio
- **File Validation**: Automatic verification that decompressed files match originals
- **Custom Data Structures**: Implements min-priority queue for efficient tree construction

## Algorithm Details

### Huffman Coding Process

1. **Frequency Analysis**: Counts the occurrence of each byte (0-255) in the input file
2. **Tree Construction**: Builds a Huffman tree using a min-priority queue:
   - Creates leaf nodes for each unique byte with its frequency
   - Repeatedly merges the two nodes with lowest frequencies
   - Continues until a single root node remains
3. **Code Generation**: Traverses the tree to assign binary codes:
   - Left child = '0', Right child = '1'
   - Shorter codes for more frequent characters
4. **Encoding**: Replaces each byte with its corresponding Huffman code
5. **Bit Packing**: Writes codes as a stream of bits (not byte-aligned)

### File Format

The compressed file (`.huf`) structure:

```
[Header Section]
- Magic bytes: "HUF1" (4 bytes) - identifies Huffman format
- Original file length (8 bytes, long)
- Count of unique bytes (4 bytes, int)
- Frequency table: For each unique byte:
  - Byte value (1 byte)
  - Frequency (4 bytes, int)

[Compressed Data Section]
- Bit stream of Huffman-encoded data
```

**Header Optimization**: Only non-zero frequencies are stored, reducing header size for files with limited character sets.

## Requirements

- **Java Development Kit (JDK)**: Version 8 or higher
- **JavaFX**: Required for the GUI components
  - JavaFX is included in JDK 8-10
  - For JDK 11+, you need to add JavaFX as a separate dependency

## Project Structure

```
src/
├── HuffmanAppFX.java        # Main GUI application (JavaFX)
├── HuffmanService.java       # Core compression/decompression service
├── HuffmanCodec.java         # Huffman tree building and code generation
├── HuffmanNode.java         # Node representation for Huffman tree
├── MinPriorityQueue.java    # Min-heap priority queue implementation
├── BitInputStream.java      # Bit-level input stream for reading compressed data
├── BitOutputStream.java     # Bit-level output stream for writing compressed data
└── FileUtil.java            # File I/O utilities and frequency counting
```

## Compilation

### Using Command Line

1. Navigate to the project directory:
```bash
cd HuffmanProject
```

2. Compile all Java files:
```bash
javac -d out src/*.java
```

3. Run the application:
```bash
java -cp out HuffmanAppFX
```

### Using an IDE

1. Import the project into IntelliJ IDEA (or your preferred IDE)
2. Ensure JavaFX is properly configured in your project settings
3. Set `HuffmanAppFX.java` as the main class
4. Run the application

## Usage

### GUI Application

1. **Launch the Application**: Run `HuffmanAppFX.java`

2. **Compress a File**:
   - Click "Browse..." next to "Input file" and select a file to compress
   - The compressed output path will auto-fill (original name + `.huf` extension)
   - Optionally modify the output path
   - Click "Compress"
   - View compression statistics in the log area
   - Examine the encoding table showing character frequencies and codes
   - Review the header structure in the header display area

3. **Decompress a File**:
   - Click "Browse..." next to "Compressed output (.huf)" and select a `.huf` file
   - The decoded output path will auto-fill (original name + `_decoded` extension)
   - Optionally modify the output path
   - Click "Decompress"
   - The application will verify that the decompressed file matches the original (if provided)

### GUI Features

- **Encoding Table**: Displays all characters with their:
  - Byte value
  - Character representation (printable characters shown)
  - Frequency count
  - Huffman code (binary string)

- **Header Display**: Shows detailed header structure including:
  - Magic bytes
  - Original file length
  - Frequency table entries
  - Header size breakdown

- **Log Area**: Provides real-time feedback:
  - Compression/decompression progress
  - File sizes (original, compressed, header, data)
  - Compression ratio
  - Verification results

## Compression Performance

### Typical Results

- **Text Files**: 40-60% compression ratio (depending on character distribution)
- **Binary Files**: Variable, often 50-80% for files with repeated patterns
- **Small Files**: May expand due to header overhead (common for files < 1KB)

### Factors Affecting Compression

1. **Character Distribution**: Files with skewed character frequencies compress better
2. **File Size**: Larger files amortize header overhead better
3. **Repetition**: Files with repeated patterns achieve higher compression
4. **Uniqueness**: Files with many unique characters have larger headers

## Technical Implementation

### Key Components

1. **HuffmanNode**: Represents nodes in the Huffman tree
   - Leaf nodes: contain byte value and frequency
   - Internal nodes: contain left/right children and combined frequency
   - Implements `Comparable` for priority queue ordering

2. **MinPriorityQueue**: Min-heap implementation for efficient tree building
   - O(log n) insertion and deletion
   - Used to repeatedly extract two lowest-frequency nodes

3. **BitInputStream/BitOutputStream**: Custom bit-level I/O
   - Reads/writes individual bits, not just bytes
   - Handles bit buffering for efficiency
   - Essential for variable-length code storage

4. **HuffmanCodec**: Core algorithm implementation
   - `buildTree()`: Constructs Huffman tree from frequency array
   - `buildCodes()`: Generates binary codes via tree traversal

5. **HuffmanService**: High-level compression/decompression
   - `compress()`: Full compression pipeline
   - `decompress()`: Full decompression pipeline
   - Header management and file format handling

## Example Workflow

```
Original File: "example.txt" (1000 bytes)
  ↓ [Frequency Analysis]
Character frequencies calculated
  ↓ [Tree Building]
Huffman tree constructed
  ↓ [Code Generation]
Variable-length codes assigned
  ↓ [Encoding]
File encoded using Huffman codes
  ↓ [Bit Packing]
Compressed to "example.huf" (600 bytes)

Compression Ratio: 0.60x (40% reduction)
Header Size: 50 bytes
Data Size: 550 bytes
```

## Algorithm Complexity

- **Time Complexity**:
  - Frequency counting: O(n) where n = file size
  - Tree building: O(k log k) where k = number of unique characters (≤ 256)
  - Encoding: O(n)
  - Decoding: O(n)
  - **Overall**: O(n + k log k) ≈ O(n) for typical files

- **Space Complexity**:
  - Frequency array: O(1) - fixed 256 entries
  - Tree: O(k) - at most 2k-1 nodes
  - Code table: O(k)
  - **Overall**: O(k) where k ≤ 256

## Error Handling

The application handles various error cases:
- Invalid file formats (non-Huffman files)
- Missing or corrupted headers
- Unexpected end of file during decompression
- File I/O errors
- Empty files (special case handling)

## Educational Value

This project demonstrates:
- **Data Structures**: Priority queues, binary trees, bit manipulation
- **Algorithms**: Greedy algorithm design, tree traversal (DFS)
- **File I/O**: Binary file handling, bit-level operations
- **GUI Development**: JavaFX application design
- **Software Engineering**: Modular design, separation of concerns

## Limitations

- **Header Overhead**: Small files may not compress well due to header size
- **Single File Format**: Only supports the custom `.huf` format
- **Memory Usage**: Entire file is processed in memory (not suitable for very large files)
- **No Streaming**: File must be fully read before compression begins

## Future Enhancements

Potential improvements:
- Adaptive Huffman coding for streaming compression
- Support for multiple compression algorithms
- Batch file processing
- Compression level settings
- Progress bars for large files
- Command-line interface option

## License

This project is open source and available for educational and research purposes.

## Author
Laith Amro
Developed as a course project demonstrating data compression algorithms and GUI development in Java.
