import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class WordCounter {
    private Map<String, Integer> wordCount;
    private final Object lock;

    public WordCounter() {
        wordCount = new ConcurrentHashMap<>();
        lock = new Object();
    }

    public void countWords(String[] words) {
        synchronized (lock) {
            for (String word : words) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
    }

    public Map<String, Integer> getWordCount() {
        return wordCount;
    }
}

class FileProcessor implements Runnable {
    private final WordCounter wordCounter;
    private final String filePath;
    private final int startPosition;
    private final int chunkSize;

    public FileProcessor(WordCounter wordCounter, String filePath, int startPosition, int chunkSize) {
        this.wordCounter = wordCounter;
        this.filePath = filePath;
        this.startPosition = startPosition;
        this.chunkSize = chunkSize;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }

            String[] words = sb.toString().split("\\P{Alpha}+"); // Split using non-alphabetic characters
            int startIdx = (startPosition - 1) * chunkSize;
            int endIdx = Math.min(startIdx + chunkSize, words.length);

            String[] chunk = Arrays.copyOfRange(words, startIdx, endIdx);
            wordCounter.countWords(chunk);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MultithreadingCountTask {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\Tayyab Anees\\OneDrive\\Desktop\\Semester Work\\Lab9\\src\\sample.txt"; // Replace with the path to your text file
        int chunkSize = 5;

        WordCounter wordCounter = new WordCounter();
        List<Thread> threads = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(" ");
            }

            String[] words = sb.toString().split("\\P{Alpha}+");
            int totalChunks = (int) Math.ceil((double) words.length / chunkSize);

            for (int i = 1; i <= totalChunks; i++) {
                FileProcessor fileProcessor = new FileProcessor(wordCounter, filePath, i, chunkSize);
                Thread thread = new Thread(fileProcessor);
                threads.add(thread);
                thread.start();
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int totalwords=0;
            Map<String, Integer> finalWordCount = wordCounter.getWordCount();
            for (Map.Entry<String, Integer> entry : finalWordCount.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
                totalwords+=entry.getValue();
            }
            System.out.println("Total Unique Words: "+finalWordCount.size());
            System.out.println("Total Words Count: "+totalwords);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
