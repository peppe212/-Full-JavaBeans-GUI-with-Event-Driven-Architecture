/**
 * Matching Pairs Game - Main Board Frame
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException; // Required for handling vetoes
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList; // Thread-safe list for listeners

/**
 * The main application window (JFrame) for the Matching Pairs game.
 * This BEAN acts as the central coordinator, creating all other component beans
 * (Cards, Controller, Counter, Challenge, Buttons), arranging them in the layout,
 * and establishing the necessary event listener connections between them according
 * to the Observer (aka Publisher/Subscriber pattern).
 * It also handles the GENERATION of SHUFFLE EVENTS.
 */
public class Board extends JFrame {

    // Configuration: Number of distinct card values (N pairs = 2*N total cards).
    private static final int N = 4;
    // Total number of cards on the board.
    private static final int TOTAL_CARDS = 2 * N;

    // --- Component Beans ---
    private final List<Card> cards = new ArrayList<>(); // Holds the Card beans
    private final Controller controller; // Game logic and pairs found display
    private final Counter counter;       // Move counter display
    private final Challenge challenge;   // Best score display (optional)
    private final JButton shuffleButton; // Button to shuffle cards
    private final JButton exitButton;    // Button to exit the game

    // A thread-safe list to store listeners that want to be notified when the cards are shuffled
    private final List<ShuffleListener> shuffleListeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs the main game Board.
     * Initializes UI, creates component beans, sets up layout, registers listeners,
     * and makes the window visible.
     */
    public Board() {
        super("Matching Pairs Game"); // Set window title

        // --- Bean Instantiation ---
        controller = new Controller(N);
        counter = new Counter(/*controller*/);
        // Challenge needs a Counter reference to get the final score
        challenge = new Challenge(counter);
        shuffleButton = new JButton("Shuffle");
        exitButton = new JButton("Exit");

        // --- UI Setup ---
        JPanel cardPanel = createCardPanel(); // Create cards and their panel
        setupListeners();                   // Wire up beans via listeners
        setupLayout(cardPanel);             // Arrange components in the frame

        // --- Window Configuration ---
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit application on close
        setSize(640, 480);                // Set window size
        setLocationRelativeTo(null);                  // Center window on screen
        setVisible(true);                             // Make the window visible

        // --- Initial Game State ---
        // Perform the first shuffle after the GUI is set up and visible
        SwingUtilities.invokeLater(this::fireShuffleEvent);
    }

    /**
     * Creates the JPanel that holds all the Card beans.
     * Also, responsible for creating the Card instances themselves.
     * Uses a GridLayout, dynamically adjusting columns based on N.
     * @return The JPanel containing the cards.
     */
    private JPanel createCardPanel() {
        // Default layout for N=4 (2 rows, 4 columns)
        JPanel cardPanel = new JPanel(new GridLayout(0, 4, 5, 5)); // rows=0 means flexible
        // Adjust layout for different N values
        if (N > 4) {
            // Simple dynamic column calculation (e.g., aim for roughly square layout)
            int cols = (int) Math.ceil(Math.sqrt(TOTAL_CARDS));
            cardPanel.setLayout(new GridLayout(0, cols, 5, 5)); // Adjust spacing as needed
        }
        // Create and add card beans
        for (int i = 0; i < TOTAL_CARDS; i++) {
            Card card = new Card(i); // Create card with its index
            cards.add(card);         // Add to internal list
            cardPanel.add(card);     // Add to the display panel
        }
        return cardPanel;
    }

    /**
     * Establishes all necessary event listener relationships between the beans.
     * This is the core of the Observer pattern implementation, connecting publishers
     * and subscribers.
     */
    private void setupListeners() {
        // --- Card Listeners ---
        for (Card card : cards) {
            // Controller listens to Card state changes (Property & Vetoable)
            card.addPropertyChangeListener(controller);
            card.addVetoableChangeListener(controller);

            // Counter listens to Card state changes (Property, for flip count)
            card.addPropertyChangeListener(counter);

            // Card listens to Board for Shuffle events
            this.addShuffleListener(card);

            // Card listens to Controller for Match events
            controller.addMatchedListener(card);

            // Add ActionListener (for each card) for user clicks directly on the Card (which is a JButton)
            card.addActionListener(e -> {
                Card sourceCard = (Card) e.getSource();
                // Rule: Ignore click if already processing OR if card is not face down
                if (controller.isProcessing() || sourceCard.getState() != CardState.FACE_DOWN) {
                    return; // Do nothing
                }
                // If click is valid, attempt to turn the card face up
                try {
                    sourceCard.setState(CardState.FACE_UP); // This triggers Property/Vetoable events
                } catch (PropertyVetoException ex) {
                    // Veto likely occurred (e.g., trying to flip a third card)
                    System.out.println("Move vetoed: " + ex.getMessage()); // Log or show message
                } catch (Exception ex) {
                    // Catch unexpected errors during state change
                    System.err.println("Unexpected error during card click: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } // End of card for loop

        // --- Other Bean Listeners ---

        // Controller and Counter listen to Board for Shuffle events (to reset)
        this.addShuffleListener(controller);
        this.addShuffleListener(counter);

        // Challenge listens to Controller for "gameFinished" PropertyChange event
        controller.addPropertyChangeListener(challenge); // Changed from counter

        // --- Button Listeners ---

        // Shuffle button click fires a shuffle event
        shuffleButton.addActionListener(e -> fireShuffleEvent());

        // Exit button click asks for confirmation and exits
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    Board.this, // Parent component
                    "Are you sure you want to exit?", // Message
                    "Confirm Exit", // Title
                    JOptionPane.YES_NO_OPTION); // Button options
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0); // Terminate the application
            }
        });
    } // end setupListeners()

    /**
     * Sets up the main layout of the JFrame, arranging the top control panel
     * and the central card panel.
     * @param cardPanel The JPanel containing the Card beans.
     */
    private void setupLayout(JPanel cardPanel) {
        // Panel for top controls (labels and buttons)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Center alignment with gaps
        topPanel.add(controller);
        topPanel.add(counter);
        topPanel.add(challenge);
        topPanel.add(shuffleButton);
        topPanel.add(exitButton); // Exit button added here

        // Main frame layout: BorderLayout
        setLayout(new BorderLayout(5, 5)); // Gaps between regions
        add(topPanel, BorderLayout.NORTH);   // Controls at the top
        add(cardPanel, BorderLayout.CENTER); // Cards in the center

        // Add some padding around the main content pane
        ((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // --- Shuffle Event Handling ---

    /**
     * Initiates the shuffle process: generates new card values and fires the
     * ShuffleEvent to all registered listeners. Ensures GUI repaint afterward.
     */
    private void fireShuffleEvent() {
        List<Integer> values = generateCardValues(); // Get new randomized values
        ShuffleEvent event = new ShuffleEvent(this, values); // Create event object
        // Notify all registered listeners
        for (ShuffleListener listener : shuffleListeners) {
            listener.shufflePerformed(event); // Listener handles the event
        }
        // Ensure the UI updates correctly after shuffling
        SwingUtilities.invokeLater(this::revalidate);
        SwingUtilities.invokeLater(this::repaint);
    }

    /**
     * Generates a list containing N pairs of integers (from 1 to N)
     * in a random order.
     * @return A shuffled List of integers for card values.
     */
    private List<Integer> generateCardValues() {
        List<Integer> values = new ArrayList<>(TOTAL_CARDS);
        // Create pairs
        for (int i = 1; i <= N; i++) {
            values.add(i);
            values.add(i);
        }
        // Shuffle the list randomly
        Collections.shuffle(values, new Random());
        return values;
    }

    // --- ShuffleListener Management Methods ---

    /**
     * Adds a listener for ShuffleEvents. Uses a thread-safe list.
     * @param listener The ShuffleListener to add.
     */
    public void addShuffleListener(ShuffleListener listener) {
        shuffleListeners.add(listener);
    }

    /**
     * Removes a listener for ShuffleEvents. Uses a thread-safe list.
     * @param listener The ShuffleListener to remove.
     */
    public void removeShuffleListener(ShuffleListener listener) {
        shuffleListeners.remove(listener);
    }

    // --- Application Entry Point ---

    /**
     * The main method to launch the Matching Pairs game.
     * Creates the Board frame on the Event Dispatch Thread (EDT).
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Ensure GUI creation and manipulation happens on the EDT
        SwingUtilities.invokeLater(Board::new);
    }
} // End of Board class