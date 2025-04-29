/**
 * Matching Pairs Game - Matched Listener Interface
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import java.util.EventListener;

/**
 * Defines the interface for objects (typically Cards) that need to react
 * to the outcome of a card matching attempt. Implementing this interface
 * allows components to subscribe to MatchedEvent notifications fired by
 * the Controller.
 */
public interface MatchedListener extends EventListener {
    /**
     * Invoked when a MatchedEvent is fired, indicating the result of comparing
     * two revealed cards. Implementers (usually the Card beans) should define
     * the logic to update their state based on whether a match occurred.
     *
     * @param evt The MatchedEvent object containing the outcome (match or mismatch).
     */
    void matchResult(MatchedEvent evt);
}