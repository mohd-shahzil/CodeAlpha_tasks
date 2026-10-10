import java.util.List;

/** One FAQ item: several ways of asking the same question plus one answer. */
public class FaqEntry {
    public final List<String> questions;
    public final String answer;

    public FaqEntry(List<String> questions, String answer) {
        this.questions = questions;
        this.answer = answer;
    }

    /** The first question is used as the "display" question for suggestions. */
    public String mainQuestion() {
        return questions.get(0);
    }
}
