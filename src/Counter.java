/**
 * Matching Pairs Game - Counter Bean
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A JavaBean that acts as a counter for the total number of card flips (moves).
 * It extends JLabel to display the current count. It listens for Card state changes
 * to increment the count when a card becomes FACE_UP. It resets on shuffle.
 * It fires PropertyChangeEvents for the "flipCount" property but no longer
 * detects or signals game completion.
 */
public class Counter extends JLabel implements Serializable, PropertyChangeListener, ShuffleListener {

    // Stores the total number of times cards have been turned face up.
    private int flipCount = 0;
    // Helper for firing PropertyChangeEvents ("flipCount").
    private PropertyChangeSupport pcs;
    // Reference to Controller removed as game end detection is moved to Controller.

    /**
     * Constructs the Counter bean.
     * Initializes the label and the PropertyChangeSupport helper.
     */
    public Counter(/* Controller parameter removed */) {
        super("Mosse: 0"); // Initial text
        // Initialize pcs after super()
        this.pcs = new PropertyChangeSupport(this);
        setHorizontalAlignment(SwingConstants.CENTER); // Center text
    }

    /**
     * Updates the JLabel text to display the current flip count.
     * Ensures the update happens on the Event Dispatch Thread (EDT).
     */
    private void updateLabel() {
        SwingUtilities.invokeLater(() -> setText("Mosse: " + flipCount));
    }

    /**
     * Resets the flip count to zero and updates the label.
     * Fires a PropertyChangeEvent for "flipCount" indicating the reset.
     */
    private void resetCounter() {
        int oldFlipCount = this.flipCount;
        flipCount = 0;
        updateLabel(); // Update display
        // Notify listeners about the reset (value changing to 0)
        if (this.pcs != null) {
            SwingUtilities.invokeLater(() -> pcs.firePropertyChange("flipCount", oldFlipCount, 0));
        }
    }

    // --- Event Listener Implementations ---

    /**
     * Handles PropertyChangeEvents, specifically listening for Card state changes to FACE_UP.
     * Increments the flip count and notifies listeners about the "flipCount" change.
     * It no longer checks for game completion.
     * @param evt The PropertyChangeEvent object.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Only interested in state changes from Card objects
        if (evt.getSource() instanceof Card && "state".equals(evt.getPropertyName())) {
            // Increment count only when a card becomes FACE_UP
            if (evt.getNewValue() == CardState.FACE_UP) {
                int oldFlipCount = this.flipCount;
                flipCount++;
                updateLabel(); // Update display

                // Notify listeners about the change in flipCount
                if (this.pcs != null) {
                    final int currentFlipCount = flipCount; // Use final variable for lambda
                    SwingUtilities.invokeLater(() -> pcs.firePropertyChange("flipCount", oldFlipCount, currentFlipCount));
                }
                // *** Game Finished check removed from here ***
            }
        }
    }

    /**
     * Handles the ShuffleEvent. Resets the counter.
     * @param evt The ShuffleEvent object.
     */
    @Override
    public void shufflePerformed(ShuffleEvent evt) {
        resetCounter();
    }

    // --- Listener Management Methods ---

    /**
     * Adds a PropertyChangeListener for "flipCount" events.
     * @param listener The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // Ensure pcs is initialized (defensive)
        if (this.pcs == null) this.pcs = new PropertyChangeSupport(this);
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener.
     * @param listener The listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.pcs != null) pcs.removePropertyChangeListener(listener);
    }

    /**
     * Gets the current flip count. Primarily used by the Challenge bean
     * after receiving the "gameFinished" signal from the Controller.
     * @return The total number of card flips in the current round.
     */
    public int getFlipCount() {
        return flipCount;
    }
}