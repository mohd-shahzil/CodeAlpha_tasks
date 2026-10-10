import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Swing chat window with message bubbles, a typing indicator and quick-reply buttons. */
public class ChatbotGUI extends JFrame {

    private static final Color BRAND = new Color(0x1E3A8A);
    private static final Color BOT_BG = new Color(0xE8EDF7);
    private static final Color USER_BG = new Color(0x2563EB);
    private static final Color WINDOW_BG = new Color(0xF8FAFC);
    private static final Font CHAT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private final ChatbotEngine engine;
    private final JPanel messages = new JPanel();
    private final JScrollPane scroll;
    private final JTextField input = new JTextField();
    private final JButton send = new JButton("Send");

    public ChatbotGUI(ChatbotEngine engine) {
        super("CodeAlpha Assistant");
        this.engine = engine;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(WINDOW_BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
        messages.setBackground(WINDOW_BG);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(WINDOW_BG);
        holder.add(messages, BorderLayout.NORTH);
        scroll = new JScrollPane(holder);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(WINDOW_BG);
        bottom.add(buildQuickReplies(), BorderLayout.NORTH);
        bottom.add(buildInputBar(), BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        addBotMessage(engine.welcome());
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BRAND);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel title = new JLabel("CodeAlpha Assistant");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel sub = new JLabel("Ask me anything about CodeAlpha internships");
        sub.setForeground(new Color(0xBFD0F5));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(title);
        text.add(sub);
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    private JComponent buildQuickReplies() {
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        chips.setBackground(WINDOW_BG);
        for (String q : engine.quickQuestions()) {
            JButton b = new JButton(q);
            b.setFont(new Font("SansSerif", Font.PLAIN, 12));
            b.setFocusPainted(false);
            b.addActionListener(e -> submit(q));
            chips.add(b);
        }
        JScrollPane sp = new JScrollPane(chips, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(100, 58));
        // Single horizontal row of chips
        chips.setLayout(new BoxLayout(chips, BoxLayout.X_AXIS));
        for (Component c : chips.getComponents()) {
            ((JComponent) c).setAlignmentY(Component.CENTER_ALIGNMENT);
        }
        chips.setBorder(new EmptyBorder(8, 8, 8, 8));
        for (int i = chips.getComponentCount() - 1; i > 0; i--) {
            chips.add(Box.createHorizontalStrut(6), i);
        }
        return sp;
    }

    private JPanel buildInputBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBorder(new EmptyBorder(8, 10, 10, 10));
        bar.setBackground(WINDOW_BG);
        input.setFont(CHAT_FONT);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCBD5E1)), new EmptyBorder(8, 10, 8, 10)));
        send.setBackground(USER_BG);
        send.setForeground(Color.WHITE);
        send.setFocusPainted(false);
        send.setFont(new Font("SansSerif", Font.BOLD, 13));
        bar.add(input, BorderLayout.CENTER);
        bar.add(send, BorderLayout.EAST);

        send.addActionListener(e -> submit(input.getText()));
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submit(input.getText());
                }
            }
        });
        return bar;
    }

    // ------------------------------------------------------------------ chat logic

    private void submit(String text) {
        String msg = text == null ? "" : text.trim();
        if (msg.isEmpty()) {
            return;
        }
        input.setText("");
        addUserMessage(msg);
        setInputEnabled(false);

        JComponent typing = addBubble("typing...", false, true);
        // small delay makes the conversation feel real-time
        Timer timer = new Timer(450, e -> {
            removeRow(typing);
            addBotMessage(engine.reply(msg));
            setInputEnabled(true);
            input.requestFocusInWindow();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void setInputEnabled(boolean enabled) {
        input.setEnabled(enabled);
        send.setEnabled(enabled);
    }

    private void addUserMessage(String text) {
        addBubble(text, true, false);
    }

    private void addBotMessage(String text) {
        addBubble(text, false, false);
    }

    private JComponent addBubble(String text, boolean fromUser, boolean italic) {
        String html = escape(text).replace("\n", "<br>");
        if (italic) {
            html = "<i>" + html + "</i>";
        }
        JLabel label = new JLabel("<html><body style='width:260px'>" + html + "</body></html>");
        label.setFont(CHAT_FONT);
        label.setForeground(fromUser ? Color.WHITE : new Color(0x1F2937));
        label.setOpaque(true);
        label.setBackground(fromUser ? USER_BG : BOT_BG);
        label.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(WINDOW_BG);
        row.setBorder(new EmptyBorder(5, 10, 5, 10));
        row.add(label, fromUser ? BorderLayout.EAST : BorderLayout.WEST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        messages.add(row);
        messages.revalidate();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
        return row;
    }

    private void removeRow(JComponent row) {
        messages.remove(row);
        messages.revalidate();
        messages.repaint();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
