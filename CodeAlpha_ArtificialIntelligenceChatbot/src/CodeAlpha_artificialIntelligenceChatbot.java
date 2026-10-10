import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Entry point.
 *   java -cp out Main          -> opens the GUI chat window
 *   java -cp out Main --cli    -> chat in the console (handy for quick tests)
 */
public class CodeAlpha_artificialIntelligenceChatbot {

    public static void main(String[] args) throws Exception {
        ChatbotEngine engine = new ChatbotEngine("faq.txt");

        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            runConsole(engine);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // default look and feel is fine
            }
            new ChatbotGUI(engine).setVisible(true);
        });
    }

    private static void runConsole(ChatbotEngine engine) throws Exception {
        System.out.println("Bot: " + engine.welcome());
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.trim().equalsIgnoreCase("exit") || line.trim().equalsIgnoreCase("quit")) {
                System.out.println("Bot: Goodbye!");
                break;
            }
            System.out.println("You: " + line);
            System.out.println("Bot: " + engine.reply(line));
        }
    }
}
