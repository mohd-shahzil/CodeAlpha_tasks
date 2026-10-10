import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Loads FAQ entries from faq.txt so the bot can be "trained" by editing a text file. */
public class KnowledgeBase {

    private final List<FaqEntry> entries = new ArrayList<>();

    public KnowledgeBase(String fileName) throws IOException {
        Path path = locate(fileName);
        load(Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    private static Path locate(String fileName) throws IOException {
        String[] candidates = {fileName, "../" + fileName, "CodeAlphaChatbot/" + fileName};
        for (String c : candidates) {
            Path p = Paths.get(c);
            if (Files.exists(p)) {
                return p;
            }
        }
        throw new IOException("Could not find " + fileName + ". Put it in the folder you run the bot from.");
    }

    private void load(List<String> lines) {
        List<String> questions = null;
        StringBuilder answer = null;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.startsWith("#")) {
                continue;
            }
            if (line.isEmpty()) {
                flush(questions, answer);
                questions = null;
                answer = null;
            } else if (line.startsWith("Q:")) {
                flush(questions, answer);
                answer = null;
                questions = new ArrayList<>();
                for (String q : line.substring(2).split("\\|\\|")) {
                    if (!q.trim().isEmpty()) {
                        questions.add(q.trim());
                    }
                }
            } else if (line.startsWith("A:")) {
                answer = new StringBuilder(line.substring(2).trim());
            } else if (answer != null) {
                answer.append(' ').append(line);
            }
        }
        flush(questions, answer);
    }

    private void flush(List<String> questions, StringBuilder answer) {
        if (questions != null && !questions.isEmpty() && answer != null && answer.length() > 0) {
            entries.add(new FaqEntry(questions, answer.toString()));
        }
    }

    public List<FaqEntry> getEntries() {
        return entries;
    }
}
