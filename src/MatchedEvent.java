/**
 * Matching Pairs Game - Matched Event Definition
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import java.util.EventObject;

/**
 * Represents an event indicating the outcome of a card matching attempt.
 * This event is fired by the Controller after two cards have been turned
 * face up and compared. It informs listeners (Cards) whether the pair
 * was a match or not.
 */
public class MatchedEvent extends EventObject {
    // Boolean flag indicating if the two revealed cards form a matching pair.
    private final boolean match;

    /**
     * Constructs a new MatchedEvent.
     *
     * @param source The object that originated the event (the game Controller).
     * @param match  {@code true} if the revealed cards were a match,
     * {@code false} otherwise.
     */
    public MatchedEvent(Object source, boolean match) {
        super(source);
        this.match = match;
    }

    /**
     * Returns the result of the matching attempt.
     * Listeners (Cards) use this to determine how to update their state
     * (e.g., become EXCLUDED on a match, or revert to FACE_DOWN on a mismatch).
     *
     * @return {@code true} if the cards matched, {@code false} otherwise.
     */
    public boolean isMatch() {
        return match;
    }
}