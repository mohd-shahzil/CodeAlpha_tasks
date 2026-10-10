# CodeAlpha AI Chatbot (Java)

A Java chatbot that answers frequently asked questions about CodeAlpha, built for the CodeAlpha Java Programming internship task.

## How it meets the task requirements

| Requirement | Where it is done |
|---|---|
| Java-based chatbot for interactive communication | `Main.java`, `ChatbotEngine.java` |
| Natural Language Processing (NLP) | `NLPProcessor.java`: tokenization, stop-word removal, synonym normalisation, stemming |
| Machine learning logic or rule-based answers | `ChatbotEngine.java`: TF-IDF vectors + cosine similarity (ML-style retrieval) and regex rules for small talk |
| Train the bot on FAQs | `faq.txt` is loaded by `KnowledgeBase.java`; `ChatbotEngine.train()` builds the model |
| GUI for real-time interaction | `ChatbotGUI.java`: Swing chat window with bubbles, typing indicator and quick-reply buttons |

## Run it

Requires JDK 11 or newer.

```
javac -d out src/*.java
java -cp out Main          # GUI window
java -cp out Main --cli    # console mode
```

Run the commands from the project folder so `faq.txt` is found. On Windows you can also double-click `run.bat`; on Linux/macOS use `./run.sh`.

## How it works

1. **Pre-processing**: the user's message is lower-cased, tokenized, stop words are removed, synonyms are normalised (for example "fee", "price" and "cost" all become "cost") and words are stemmed.
2. **Rules first**: greetings, thanks, goodbye, "my name is ...", time/date, jokes and help are matched with regex patterns.
3. **FAQ retrieval**: every question in `faq.txt` is turned into a TF-IDF vector. The user's message is turned into a vector the same way and compared with cosine similarity. Above 0.35 the bot answers; between 0.15 and 0.35 it suggests "Did you mean...?"; below that it gives a polite fallback.

## Training the bot with new FAQs

Open `faq.txt` and add an entry:

```
Q: question one || another way to ask it || a third way
A: The answer.
```

Restart the bot. No code changes are needed. More question variants means better matching.

## Notes

The FAQ answers are based on publicly available CodeAlpha internship material. Verify the details against www.codealpha.tech and edit `faq.txt` where they differ for your program.
