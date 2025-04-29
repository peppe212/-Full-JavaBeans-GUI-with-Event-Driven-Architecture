/**
 * Matching Pairs Game - Card Bean
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import javax.swing.*;
import java.awt.*;
import java.beans.*; // Required for PropertyChangeSupport, VetoableChangeSupport, etc.
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single card in the Matching Pairs game. This class acts as a JavaBean,
 * extending JButton for its visual representation and interaction. It manages its own
 * integer 'value' and 'state' (FACE_DOWN, FACE_UP, EXCLUDED). The 'state' property
 * is both bound (notifies listeners of changes) and constrained (allows listeners
 * to veto changes). It also listens for Shuffle and Match events.
 */
public class Card extends JButton implements Serializable, ShuffleListener, MatchedListener {

    // The integer value displayed when the card is FACE_UP. Pairs ofc have the same value.
    private int value;
    // The current state of the card (FACE_DOWN, FACE_UP, or EXCLUDED). Bound and Constrained property.
    private CardState state;
    // The unique index of this card on the board, used to retrieve its value during shuffle.
    private final int cardIndex;

    // Helper objects for managing property change listeners (Bound properties).
    private PropertyChangeSupport pcs;
    // Helper objects for managing vetoable change listeners (Constrained properties).
    private VetoableChangeSupport vcs;

    /**
     * Constructs a Card bean. Initializes the card in the FACE_DOWN state.
     * PropertyChangeSupport and VetoableChangeSupport are initialized here,
     * after the superclass constructor call, to prevent issues with Swing's UI delegate initialization.
     *
     * @param index The zero-based index of this card on the game board.
     */
    public Card(int index) {
        super(); // Call JButton constructor
        // Initialize listener support objects after the superclass constructor
        this.pcs = new PropertyChangeSupport(this);
        this.vcs = new VetoableChangeSupport(this);

        this.cardIndex = index;

        // Set the initial state directly and update appearance
        this.state = CardState.FACE_DOWN;
        updateAppearance(); // Apply initial visual state

        // Configure button appearance
        setFont(new Font("Arial", Font.BOLD, 24));
        setPreferredSize(new Dimension(80, 120)); // Set preferred size
    }

    // --- 'value' Property Handling (Read-only externally) ---

    /**
     * Gets the integer value of this card.
     * @return The card's integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Internal method to set the card's value. Typically called during shuffle.
     * Updates the button text only if the card is currently FACE_UP.
     * @param newValue The new integer value for the card.
     */
    private void setValueInternal(int newValue) {
        this.value = newValue;
        if (this.state == CardState.FACE_UP) {
            updateAppearance();
        }
    }

    // --- 'state' Property Handling (Bound and Constrained) ---

    /**
     * Gets the current state of this card (FACE_DOWN, FACE_UP, or EXCLUDED).
     * @return The current CardState.
     */
    public CardState getState() {
        return state;
    }

    /**
     * Attempts to set the state of this card. This method enforces the constrained
     * property logic by first firing a VetoableChangeEvent. If no listener vetoes
     * the change, the state is updated, the appearance is changed, and a
     * PropertyChangeEvent is fired to notify bound listeners.
     *
     * @param newState The proposed new CardState.
     * @throws PropertyVetoException if a registered VetoableChangeListener vetoes the state change.
     */
    public void setState(CardState newState) throws PropertyVetoException {
        // Avoid unnecessary changes and event firing if state is already the target state.
        if (this.state == newState) return;

        CardState oldState = this.state;

        // 1. Fire vetoable change event. COSTRAINED listeners (e.g. Controller) can throw PropertyVetoException.
        // Defensive check for vcs proper initialization
        if (this.vcs == null)
            this.vcs = new VetoableChangeSupport(this);
        vcs.fireVetoableChange("state", oldState, newState);

        // 2. If no veto occurred, proceed with the state change.
        this.state = newState;
        updateAppearance(); // Update visual representation based on the new state.

        // 3. Fire property change event to notify BOUND listeners.
        // Ensure pcs is initialized (defensive check).
        if (this.pcs == null) this.pcs = new PropertyChangeSupport(this);
        pcs.firePropertyChange("state", oldState, newState);
    }

    /**
     * Updates the visual appearance of the JButton based on the current card state.
     * Sets background color, text (value or empty), and enabled status.
     * Ensures repaint request is processed.
     */
    private void updateAppearance() {
        setBackground(state.getColor());
        switch (state) {
            case FACE_UP:
                setText(String.valueOf(value)); // Show value
                setEnabled(true); // Card can potentially be interacted with (though Controller might veto)
                break;
            case FACE_DOWN:
                setText(""); // Hide value
                setEnabled(true); // Card can be clicked
                break;
            case EXCLUDED:
                setText(""); // Hide value
                setEnabled(false); // Card is out of play, disable interaction
                break;
        }
        // Force the component to re-layout and repaint
        revalidate();
        repaint();
    }

    // --- Listener Management Methods (JavaBeans Pattern) ---

    /**
     * Adds a PropertyChangeListener to the listener list.
     * The listener will be notified whenever the bound 'state' property changes.
     * @param listener The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.pcs == null) this.pcs = new PropertyChangeSupport(this);
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param listener The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.pcs != null) pcs.removePropertyChangeListener(listener);
    }

    /**
     * Adds a VetoableChangeListener to the listener list.
     * The listener will be notified before the constrained 'state' property changes,
     * allowing it to veto the change.
     * @param listener The VetoableChangeListener to be added.
     */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        if (this.vcs == null) this.vcs = new VetoableChangeSupport(this);
        vcs.addVetoableChangeListener(listener);
    }

    /**
     * Removes a VetoableChangeListener from the listener list.
     * @param listener The VetoableChangeListener to be removed.
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (this.vcs != null) vcs.removeVetoableChangeListener(listener);
    }

    // --- Game Event Listener Implementations ---

    /**
     * Handles the ShuffleEvent fired by the Board.
     * Retrieves the card's new value based on its index and resets its state to FACE_DOWN.
     * UI updates are performed on the Event Dispatch Thread (EDT).
     * @param evt The ShuffleEvent containing the new value list.
     */
    @Override
    public void shufflePerformed(ShuffleEvent evt) {
        // Assign the new value based on the card's index
        if (evt.getValues() != null && cardIndex < evt.getValues().size()) {
            setValueInternal(evt.getValues().get(cardIndex));
        }

        // Reset state to FACE_DOWN, ensuring thread safety for UI updates
        CardState oldState = this.state;
        if (oldState != CardState.FACE_DOWN) {
            this.state = CardState.FACE_DOWN;
            // Update appearance on the EDT
            SwingUtilities.invokeLater(this::updateAppearance);
            // Notify listeners of the state change on the EDT
            if (this.pcs != null) {
                SwingUtilities.invokeLater(() -> pcs.firePropertyChange("state", oldState, this.state));
            }
        } else {
            // Ensure appearance is correct even if state didn't change (e.g., initial shuffle)
            SwingUtilities.invokeLater(this::updateAppearance);
        }
    }

    /**
     * Handles the MatchedEvent fired by the Controller.
     * If the card is currently FACE_UP, it changes its state to EXCLUDED if
     * it was part of a match, or back to FACE_DOWN if it was not.
     * State changes are performed on the Event Dispatch Thread (EDT).
     * @param evt The MatchedEvent indicating the outcome (match or mismatch).
     */
    @Override
    public void matchResult(MatchedEvent evt) {
        // Ensure GUI updates happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Only react if the card is currently face up
            if (this.state == CardState.FACE_UP) {
                // Debugging output (can be removed for final version)
                // System.out.println("Card " + getValue() + ": matchResult(" + evt.isMatch() + ") - Current State: FACE_UP");
                try {
                    if (evt.isMatch()) {
                        // Debugging output (can be removed for final version)
                        // System.out.println("--> Card " + getValue() + ": Setting state EXCLUDED");
                        setState(CardState.EXCLUDED); // Matched pair: exclude card
                    } else {
                        // Debugging output (can be removed for final version)
                        // System.out.println("--> Card " + getValue() + ": Setting state FACE_DOWN");
                        setState(CardState.FACE_DOWN); // Mismatched pair: flip back down
                    }
                } catch (PropertyVetoException e) {
                    // This veto should ideally not happen here as the Controller allows these changes.
                    System.err.println("!!! UNEXPECTED VETO in matchResult for Card " + getValue() + ": " + e.getMessage());
                } catch (Exception e) {
                    // Catch any other unexpected errors during state change.
                    System.err.println("!!! UNEXPECTED ERROR in matchResult for Card " + getValue() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // --- Object Identity Methods ---

    /**
     * Checks if this Card object is equal to another object.
     * Equality is based solely on the card's unique index on the board.
     * @param o The object to compare with.
     * @return true if the objects represent the same card index, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        // if in memory it is the same object, returns true
        if (this == o) return true;
        // if o is null or not an instance of Card returns false
        if (o == null || getClass() != o.getClass()) return false;
        // equality based on index
        Card card = (Card) o;
        return cardIndex == card.cardIndex;
    }

    /**
     * Generates a hash code for this Card object.
     * Based solely on the card's unique index.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(cardIndex); // Hash code based on index
    }
}