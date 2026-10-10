import java.util.*;

/**
 * Lightweight NLP pipeline:
 * 1. Normalisation (lower-case, strip punctuation)
 * 2. Tokenisation
 * 3. Stop-word removal
 * 4. Synonym normalisation
 * 5. Simple rule-based stemming
 */
public class NLPProcessor {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "am", "do", "does", "did",
            "i", "me", "my", "you", "your", "we", "our", "us", "it", "its", "this", "that", "these",
            "those", "to", "of", "in", "on", "at", "for", "from", "with", "by", "about", "as", "and",
            "or", "but", "if", "so", "can", "could", "would", "should", "will", "shall", "may",
            "please", "tell", "know", "want", "need", "like", "get", "there", "any", "some",
            "has", "had", "what", "which", "who", "whom", "how", "when", "where", "why", "also",
            "just", "much", "many", "ok", "okay", "hey", "hi", "hello"));

    /** Maps different words with the same meaning to one canonical token. */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        put("apply", "application", "register", "registration", "enroll", "enrol", "enrollment",
                "signup", "join", "admission", "apply");
        put("cost", "price", "fee", "fees", "charge", "charges", "payment", "pay", "paid", "money",
                "free", "cost");
        put("certificate", "certificat", "certification", "certified", "cert", "diploma");
        put("duration", "long", "length", "deadline", "days", "timeframe", "period", "weeks",
                "months", "duration");
        put("contact", "reach", "support", "helpline", "mail", "email", "phone", "call", "number",
                "contact");
        put("job", "placement", "hire", "hiring", "career", "employment", "job");
        put("company", "firm", "organisation", "organization", "company");
        put("intern", "internship", "internships", "interns", "trainee", "training", "intern");
        put("task", "assignment", "assignments", "project", "projects", "work", "task");
        put("submit", "submission", "upload", "turn", "submit");
        put("website", "site", "web", "link", "url", "website");
        put("benefit", "perk", "perks", "benefits", "advantage", "advantages", "benefit");
        put("fake", "genuine", "real", "authentic", "verify", "verification", "valid", "fake");
        put("domain", "field", "track", "stream", "domains", "domain");
        put("eligible", "eligibility", "qualification", "qualify", "criteria", "requirement",
                "requirements", "eligible");
        put("mentor", "mentorship", "guidance", "doubt", "doubts", "guide", "mentor");
        put("remote", "online", "virtual", "wfh", "remote");
        put("offer", "have", "provide", "available", "offering", "offered", "offer");
        put("bot", "chatbot", "robot", "assistant", "ai", "bot");
    }

    private static void put(String canonical, String... words) {
        for (String w : words) {
            SYNONYMS.put(w, canonical);
        }
    }

    /** Splits text into lower-case alphanumeric tokens. */
    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null) {
            return tokens;
        }
        String cleaned = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9@+#.\\s]", " ")
                .replaceAll("(?<![a-z0-9])\\.|\\.(?![a-z0-9])", " ");
        for (String t : cleaned.split("\\s+")) {
            if (!t.isEmpty()) {
                tokens.add(t);
            }
        }
        return tokens;
    }

    /** Full pipeline: tokenise, remove stop words, normalise and stem. */
    public static List<String> process(String text) {
        List<String> result = new ArrayList<>();
        for (String token : tokenize(text)) {
            if (STOP_WORDS.contains(token)) {
                continue;
            }
            String t = SYNONYMS.getOrDefault(token, token);
            if (t.equals(token)) {
                t = stem(token);
                t = SYNONYMS.getOrDefault(t, t);
            }
            result.add(t);
        }
        return result;
    }

    /** Very small suffix-stripping stemmer (enough for FAQ matching). */
    static String stem(String w) {
        if (w.length() <= 3) {
            return w;
        }
        if (w.endsWith("ies") && w.length() > 4) {
            w = w.substring(0, w.length() - 3) + "y";
        } else if (w.endsWith("ied") && w.length() > 4) {
            w = w.substring(0, w.length() - 3) + "y";
        } else if (w.endsWith("sses") || w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("xes")) {
            w = w.substring(0, w.length() - 2);
        } else if (w.endsWith("ing") && w.length() > 5) {
            w = undouble(w.substring(0, w.length() - 3));
        } else if (w.endsWith("ed") && w.length() > 4) {
            w = undouble(w.substring(0, w.length() - 2));
        } else if (w.endsWith("ly") && w.length() > 5) {
            w = w.substring(0, w.length() - 2);
        } else if (w.endsWith("s") && !w.endsWith("ss") && !w.endsWith("us")) {
            w = w.substring(0, w.length() - 1);
        }
        if (w.length() > 3 && w.endsWith("e")) {
            w = w.substring(0, w.length() - 1);
        }
        return w;
    }

    private static String undouble(String w) {
        int n = w.length();
        if (n > 2 && w.charAt(n - 1) == w.charAt(n - 2) && "bdgmnprt".indexOf(w.charAt(n - 1)) >= 0) {
            return w.substring(0, n - 1);
        }
        return w;
    }
}
