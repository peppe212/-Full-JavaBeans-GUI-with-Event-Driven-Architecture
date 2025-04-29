/**
 * Matching Pairs Game - Challenge Bean (Best Score)
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * An optional JavaBean that displays the best score (lowest number of moves/flips)
 * achieved since the game started. It extends JLabel for display.
 * It implements PropertyChangeListener to listen for the "gameFinished" event
 * fired by the Controller bean. Upon receiving this event, it retrieves the final
 * score from the Counter bean and updates the best score if the current game's
 * score is lower.
 */
public class Challenge extends JLabel implements Serializable, PropertyChangeListener {

    // Stores the best score recorded so far. Initialized to max value.
    private int bestScore = Integer.MAX_VALUE;
    // Reference to the Counter bean needed to fetch the final score upon game completion.
    private final Counter counter;

    /**
     * Constructs the Challenge bean. Requires a reference to the Counter bean.
     * @param counter The Counter bean instance used to get the final flip count. Cannot be null.
     * @throws IllegalArgumentException if counter is null.
     */
    public Challenge(Counter counter) {
        super("Miglior Punteggio: N/A"); // Initial text
        setHorizontalAlignment(SwingConstants.CENTER);
        if (counter == null) { // Check for null dependency
            throw new IllegalArgumentException("Counter cannot be null for Challenge");
        }
        this.counter = counter; // Store the reference
    }

    /**
     * Updates the JLabel text to display the current best score.
     * Shows "N/A" if no game has been completed yet.
     * Ensures the update occurs on the Event Dispatch Thread (EDT).
     */
    private void updateLabel() {
        SwingUtilities.invokeLater(() -> {
            if (bestScore == Integer.MAX_VALUE) {
                setText("Miglior Punteggio: N/A");
            } else {
                setText("Miglior Punteggio: " + bestScore);
            }
        });
    }

    /**
     * Handles PropertyChangeEvents, specifically listening for the "gameFinished"
     * event fired by the Controller. When received, it gets the final score
     * from the associated Counter and updates the best score if necessary.
     * @param evt The PropertyChangeEvent object.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Listen only for the "gameFinished" event from the Controller
        if ("gameFinished".equals(evt.getPropertyName()) && evt.getSource() instanceof Controller) {
            // Game finished signal received, get the score from the Counter.
            int currentScore = this.counter.getFlipCount();

            // Compare with the current best score and update if better.
            if (currentScore < bestScore) {
                bestScore = currentScore;
                updateLabel(); // Update the display
            }
            // Otherwise, do nothing (current score wasn't better).
        }
        // Ignore all other property change events.
    }

    /**
     * Gets the best score recorded so far.
     * @return The lowest flip count achieved to complete a game, or Integer.MAX_VALUE if none completed.
     */
    public int getBestScore() {
        return bestScore;
    }
}