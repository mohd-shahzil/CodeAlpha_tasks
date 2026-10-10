import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The brain of the bot. It combines two techniques:
 *
 * 1. Rule-based answers (regex patterns) for small talk: greetings, thanks, goodbye, name, time...
 * 2. A machine-learning style retriever: TF-IDF vectors + cosine similarity between the user's
 *    question and every trained FAQ question, after NLP pre-processing.
 */
public class ChatbotEngine {

    private static final double CONFIDENT = 0.35; // answer directly
    private static final double POSSIBLE = 0.15;  // offer "did you mean" suggestions

    private static final Pattern GREETING = Pattern.compile(
            "^(hi+|hello+|hey+|hola|namaste|yo|good (morning|afternoon|evening))( there| bot| codealpha| assistant)?$");
    private static final Pattern HOW_ARE_YOU = Pattern.compile("how are you|how r u|how do you do|whats up|what s up");
    private static final Pattern THANKS = Pattern.compile("\\b(thanks|thank you|thankyou|thx|tysm)\\b");
    private static final Pattern BYE = Pattern.compile("^(bye|goodbye|good night|see you|see ya|exit|quit)\\b.*");
    private static final Pattern MY_NAME = Pattern.compile("(?:my name is|call me|this is) ([a-z]+)");
    private static final Pattern ASK_NAME = Pattern.compile("what is my name|do you know my name|who am i");
    private static final Pattern TIME = Pattern.compile(
            "what( s| is) the (current )?time|what time is it|current time|time now|time right now");
    private static final Pattern DATE = Pattern.compile(
            "(today s|todays|what is the|what s the|current) date|what day is it|what is today");
    private static final Pattern JOKE = Pattern.compile("\\bjoke\\b|make me laugh");
    private static final Pattern HELP = Pattern.compile("^(help|menu|options|commands|what can i ask)$");

    private static final String[] JOKES = {
            "Why do Java developers wear glasses? Because they don't C#!",
            "A programmer's partner says: 'Go to the store and buy a loaf of bread. If they have eggs, buy a dozen.' The programmer comes home with 12 loaves of bread.",
            "There are 10 types of people: those who understand binary and those who don't."
    };

    private final List<FaqEntry> entries;
    private final List<Doc> docs = new ArrayList<>();
    private final Map<String, Double> idf = new HashMap<>();
    private double unseenIdf;
    private final Random random = new Random();
    private String userName = null;

    private static class Doc {
        Map<String, Double> vec = new HashMap<>();
        double norm;
        int entryIndex;
    }

    public ChatbotEngine(String faqFile) throws IOException {
        this.entries = new KnowledgeBase(faqFile).getEntries();
        train();
    }

    // ------------------------------------------------------------------ training

    /** "Training": compute IDF weights and TF-IDF vectors for every FAQ question. */
    private void train() {
        List<List<String>> tokenized = new ArrayList<>();
        List<Integer> owner = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            for (String q : entries.get(i).questions) {
                tokenized.add(NLPProcessor.process(q));
                owner.add(i);
            }
        }
        int n = tokenized.size();
        Map<String, Integer> df = new HashMap<>();
        for (List<String> tokens : tokenized) {
            for (String t : new HashSet<>(tokens)) {
                df.merge(t, 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> e : df.entrySet()) {
            idf.put(e.getKey(), Math.log((n + 1.0) / (e.getValue() + 1.0)) + 1.0);
        }
        unseenIdf = Math.log(n + 1.0) + 1.0;

        for (int i = 0; i < n; i++) {
            Doc d = new Doc();
            d.entryIndex = owner.get(i);
            d.vec = vectorize(tokenized.get(i));
            d.norm = norm(d.vec);
            docs.add(d);
        }
    }

    private Map<String, Double> vectorize(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();
        for (String t : tokens) {
            tf.merge(t, 1.0, Double::sum);
        }
        Map<String, Double> vec = new HashMap<>();
        for (Map.Entry<String, Double> e : tf.entrySet()) {
            vec.put(e.getKey(), e.getValue() * idf.getOrDefault(e.getKey(), unseenIdf));
        }
        return vec;
    }

    private static double norm(Map<String, Double> vec) {
        double s = 0;
        for (double v : vec.values()) {
            s += v * v;
        }
        return Math.sqrt(s);
    }

    // ------------------------------------------------------------------ answering

    public String welcome() {
        return "Hi! I'm the CodeAlpha Assistant. Ask me about internships, certificates, tasks, "
                + "deadlines or how to contact CodeAlpha. Type 'help' to see what I can do.";
    }

    public String reply(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Please type a question and I'll do my best to help.";
        }
        String norm = String.join(" ", NLPProcessor.tokenize(input));

        String rule = ruleBasedReply(norm);
        if (rule != null) {
            return rule;
        }
        return faqReply(input);
    }

    private String ruleBasedReply(String norm) {
        Matcher m = MY_NAME.matcher(norm);
        if (m.find()) {
            String n = m.group(1);
            userName = Character.toUpperCase(n.charAt(0)) + n.substring(1);
            return "Nice to meet you, " + userName + "! How can I help you today?";
        }
        if (ASK_NAME.matcher(norm).find()) {
            return userName == null ? "I don't know your name yet. Tell me with 'my name is ...'."
                    : "Your name is " + userName + ".";
        }
        if (GREETING.matcher(norm).matches()) {
            return (userName == null ? "Hello!" : "Hello " + userName + "!")
                    + " Welcome to CodeAlpha. What would you like to know?";
        }
        if (HOW_ARE_YOU.matcher(norm).find()) {
            return "I'm doing great, thanks for asking! How can I help you with CodeAlpha today?";
        }
        if (THANKS.matcher(norm).find()) {
            return "You're welcome! Let me know if you have any other questions.";
        }
        if (BYE.matcher(norm).matches()) {
            return "Goodbye" + (userName == null ? "" : ", " + userName)
                    + "! Good luck with your internship at CodeAlpha.";
        }
        if (TIME.matcher(norm).find()) {
            return "The current time is " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + ".";
        }
        if (DATE.matcher(norm).find()) {
            return "Today is " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")) + ".";
        }
        if (JOKE.matcher(norm).find()) {
            return JOKES[random.nextInt(JOKES.length)];
        }
        if (HELP.matcher(norm).matches()) {
            return "You can ask me things like:\n"
                    + "- What is CodeAlpha?\n- How do I apply for an internship?\n"
                    + "- How do I get my certificate?\n- How do I submit my tasks?\n"
                    + "- How can I contact CodeAlpha?";
        }
        return null;
    }

    private String faqReply(String input) {
        List<String> tokens = NLPProcessor.process(input);
        if (tokens.isEmpty()) {
            return "Could you tell me a bit more? For example: \"How do I apply for the internship?\"";
        }
        Map<String, Double> q = vectorize(tokens);
        double qNorm = norm(q);

        // best score per FAQ entry
        double[] best = new double[entries.size()];
        for (Doc d : docs) {
            double dot = 0;
            for (Map.Entry<String, Double> e : q.entrySet()) {
                Double w = d.vec.get(e.getKey());
                if (w != null) {
                    dot += e.getValue() * w;
                }
            }
            double score = (qNorm == 0 || d.norm == 0) ? 0 : dot / (qNorm * d.norm);
            best[d.entryIndex] = Math.max(best[d.entryIndex], score);
        }

        Integer[] order = new Integer[entries.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> Double.compare(best[b], best[a]));

        double top = best[order[0]];
        if (top >= CONFIDENT) {
            return entries.get(order[0]).answer;
        }
        if (top >= POSSIBLE) {
            StringBuilder sb = new StringBuilder("I'm not completely sure I understood. Did you mean:\n");
            for (int i = 0; i < Math.min(3, order.length); i++) {
                if (best[order[i]] >= POSSIBLE) {
                    sb.append("- ").append(entries.get(order[i]).mainQuestion()).append('\n');
                }
            }
            return sb.toString().trim();
        }
        return "Sorry, I don't have an answer for that yet. You can ask me about CodeAlpha internships, "
                + "certificates, tasks or contact details, or email support@codealpha.tech for help.";
    }

    /** Questions shown as quick-reply buttons in the GUI. */
    public List<String> quickQuestions() {
        List<String> list = new ArrayList<>();
        for (FaqEntry e : entries) {
            if (list.size() < 6) {
                list.add(e.mainQuestion());
            }
        }
        return list;
    }

    public int faqCount() {
        return entries.size();
    }
}
