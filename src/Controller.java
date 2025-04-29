/**
 * Matching Pairs Game - Controller Bean
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import javax.swing.*;
import java.beans.*; // Required for PropertyChangeSupport, PropertyChangeEvent, VetoableChangeListener, etc.
import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList; // Thread-safe list for listeners

/**
 * Acts as the central logic controller for the Matching Pairs game.
 * This bean extends JLabel to display the count of matched pairs.
 * It implements PropertyChangeListener to monitor Card state changes,
 * VetoableChangeListener to enforce game rules (preventing invalid Card state changes),
 * and ShuffleListener to reset its state upon shuffling.
 * It manages the matching logic, delays, and fires MatchedEvent to update Cards
 * and PropertyChangeEvent ("gameFinished") to notify listeners (like Challenge)
 * when the game is won.
 */
public class Controller extends JLabel implements Serializable, PropertyChangeListener, VetoableChangeListener, ShuffleListener {

    // Counter for successfully matched pairs.
    private int pairsFound = 0;
    // Holds the first card revealed in the current turn.
    private Card firstCard = null;
    // Holds the second card revealed in the current turn.
    private Card secondCard = null;
    // Flag to prevent actions (like clicking cards) while a match check is in progress. Marked volatile for thread visibility.
    private volatile boolean processing = false;
    // Thread-safe list holding listeners interested in the MatchedEvent (typically Card instances).
    private final List<MatchedListener> matchedListeners = new CopyOnWriteArrayList<>();
    // The total number of pairs required to win the game (derived from N in Board).
    private final int totalPairs;

    // Support object for firing PropertyChangeEvents (e.g., "gameFinished").
    private PropertyChangeSupport pcs;

    /**
     * Constructs the Controller bean.
     * Initializes the label text and the PropertyChangeSupport helper.
     * @param totalPairs The total number of pairs to find to complete the game.
     */
    public Controller(int totalPairs) {
        super("Coppie trovate: 0 / " + totalPairs); // Initial label text
        this.totalPairs = totalPairs;
        setHorizontalAlignment(SwingConstants.CENTER); // Center the label text
        // Initialize listener support after superclass constructor
        this.pcs = new PropertyChangeSupport(this);
    }

    // --- Game Logic Management ---

    /**
     * Resets the Controller's state, typically called after a shuffle.
     * Resets matched pairs count, clears selected cards, and resets the processing flag.
     */
    private void resetGame() {
        pairsFound = 0;
        firstCard = null;
        secondCard = null;
        processing = false;
        updateLabel(); // Update the displayed count
    }

    /**
     * Updates the text of this JLabel to show the current progress (pairs found / total pairs).
     * Ensures the update occurs on the Event Dispatch Thread (EDT).
     */
    private void updateLabel() {
        SwingUtilities.invokeLater(() -> setText("Coppie trovate: " + pairsFound + " / " + totalPairs));
    }

    /**
     * Checks if the game has been completed (all pairs found).
     * @return true if all pairs have been found, false otherwise.
     */
    public boolean isGameFinished() {
        return pairsFound == totalPairs;
    }

    /**
     * Checks if the controller is currently processing a potential match (during the 0.5s delay).
     * Used by Board to prevent clicks during this phase.
     * @return true if processing, false otherwise.
     */
    public boolean isProcessing() {
        return processing;
    }

    /**
     * Initiates the check for a match after the second card is revealed.
     * Uses a Timer to introduce a delay, allowing the player to see the second card.
     * After the delay, it determines if the values match, updates game state,
     * fires the MatchedEvent, and potentially fires the "gameFinished" event.
     */
    private void checkForMatch() {
        // processing is already true at this point
        Timer timer = new Timer("MatchCheckTimer", true); // Use a daemon timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Store references before resetting
                Card card1 = firstCard;
                Card card2 = secondCard;
                boolean match = (card1 != null && card2 != null && card1.getValue() == card2.getValue());

                boolean gameJustFinished = false;
                if (match) {
                    pairsFound++;
                    updateLabel(); // Update pairs count display
                    if (isGameFinished()) { // Check for game completion *after* updating pairs count
                        gameJustFinished = true;
                    }
                }

                // Reset turn variables *before* notifying cards, allowing new interactions sooner
                firstCard = null;
                secondCard = null;
                processing = false;

                // Notify cards about the match result (they will update their state)
                fireMatchedEvent(match);

                // If the game just finished, notify listeners (e.g., Challenge bean)
                if (gameJustFinished) {
                    if (Controller.this.pcs != null) {
                        // Fire "gameFinished" event on the EDT
                        SwingUtilities.invokeLater(() ->
                                Controller.this.pcs.firePropertyChange("gameFinished", false, true) // Value indicates completion
                        );
                    }
                    // Show a confirmation dialog to the player
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(null, "Hai vinto!", "Gioco Terminato", JOptionPane.INFORMATION_MESSAGE)
                    );
                }
                // No need to explicitly cancel a daemon timer
            }
        }, 500); // 500 millisecond delay
    }

    // --- Event Listener Implementations ---

    /**
     * Handles PropertyChangeEvents, primarily listening for Card state changes to FACE_UP.
     * Manages the selection of the first and second cards for matching.
     * @param evt The PropertyChangeEvent object.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Listen only for 'state' property changes to FACE_UP from Card objects
        if ("state".equals(evt.getPropertyName()) &&
                evt.getNewValue() == CardState.FACE_UP &&
                evt.getSource() instanceof Card)
        {
            // Ignore if already processing a match
            if (processing) return;

            Card changedCard = (Card) evt.getSource();

            // Handle first card selection
            if (firstCard == null) {
                firstCard = changedCard;
            }
            // Handle second card selection (must be different from the first)
            else if (secondCard == null && !changedCard.equals(firstCard)) {
                secondCard = changedCard;
                processing = true; // Enter processing state
                checkForMatch(); // Start the matching process
            }
            // Ignore clicks on third card while two are already FACE_UP (veto should also prevent this)
        }
    }

    /**
     * Handles VetoableChangeEvents, specifically for Card 'state' changes.
     * Prevents invalid state transitions according to game rules:
     * - Cannot change state of an EXCLUDED card.
     * - Cannot reveal a third card if two are already FACE_UP.
     * - Cannot reveal a card while a match check is processing.
     * @param evt The PropertyChangeEvent representing the proposed state change.
     * @throws PropertyVetoException if the proposed change violates game rules.
     */
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        // Only interested in 'state' property changes
        if (!"state".equals(evt.getPropertyName())) return;

        Card sourceCard = (Card) evt.getSource();
        CardState oldState = (CardState) evt.getOldValue();
        CardState newState = (CardState) evt.getNewValue();

        // Rule 1: Cannot change state of an EXCLUDED card.
        if (oldState == CardState.EXCLUDED) {
            throw new PropertyVetoException("The card is excluded from the game.", evt);
        }

        // Rule 2: Handle attempts to turn a card FACE_UP (from FACE_DOWN)
        if (oldState == CardState.FACE_DOWN && newState == CardState.FACE_UP) {
            // Veto if trying to flip a third card when two are already selected and not processing match yet
            if (!processing && firstCard != null && secondCard != null) {
                throw new PropertyVetoException("You can only reveal two cards at a time.", evt);
            }
            // Veto if trying to flip any card while match checking is in progress (timer running)
            if (processing) {
                throw new PropertyVetoException("Please wait for the pair check to complete.", evt);
            }
        }
        // Note: Clicks on already FACE_UP or EXCLUDED cards are prevented by the ActionListener logic in Board,
        // so no explicit veto is needed here for those user actions. Programmatic changes
        // (like FACE_UP -> FACE_DOWN/EXCLUDED after match check) must be allowed.
    }

    /**
     * Handles the ShuffleEvent. Resets the controller's game state.
     * @param evt The ShuffleEvent object.
     */
    @Override
    public void shufflePerformed(ShuffleEvent evt) {
        resetGame(); // Reset game state for the new round
    }

    // --- MatchedEvent Listener Management ---

    /**
     * Adds a listener for MatchedEvents. Uses a thread-safe list.
     * @param listener The MatchedListener to add (typically a Card).
     */
    public void addMatchedListener(MatchedListener listener) {
        matchedListeners.add(listener);
    }

    /**
     * Removes a listener for MatchedEvents. Uses a thread-safe list.
     * @param listener The MatchedListener to remove.
     */
    public void removeMatchedListener(MatchedListener listener) {
        matchedListeners.remove(listener);
    }

    /**
     * Fires a MatchedEvent to all registered listeners.
     * Called by checkForMatch after the delay.
     * @param match The result of the match comparison (true if matched, false otherwise).
     */
    protected void fireMatchedEvent(boolean match) {
        MatchedEvent event = new MatchedEvent(this, match);
        // Iterate over the thread-safe list
        for (MatchedListener listener : matchedListeners) {
            listener.matchResult(event); // Card's matchResult uses invokeLater for safety
        }
    }

    // --- PropertyChangeEvent Listener Management (for "gameFinished") ---

    /**
     * Adds a PropertyChangeListener. Used by Challenge bean to listen for "gameFinished".
     * @param listener The listener to add.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        // Ensure pcs is initialized (defensive)
        if (this.pcs == null) this.pcs = new PropertyChangeSupport(this);
        this.pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener.
     * @param listener The listener to remove.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.pcs != null) this.pcs.removePropertyChangeListener(listener);
    }
}