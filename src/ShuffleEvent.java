/**
 * Matching Pairs Game - Shuffle Event Definition
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import java.util.EventObject;
import java.util.List;

/**
 * Represents an event indicating that the game cards should be shuffled
 * and assigned new values. This event is part of the Observer pattern,
 * carrying the necessary data (the new value sequence) from the publisher
 * (Board) to the subscribers (Cards).
 */
public class ShuffleEvent extends EventObject {
    // The sequence of integer values to be assigned to the cards.
    private final List<Integer> values;

    /**
     * Constructs a new ShuffleEvent.
     *
     * @param source The object that originated the event (typically the game Board).
     * @param values The List of integer values generated for the cards. This list
     * should contain pairs of identical values, randomly ordered.
     */
    public ShuffleEvent(Object source, List<Integer> values) {
        super(source);
        this.values = values;
    }

    /**
     * Returns the list of integer values associated with this shuffle event.
     * Listeners (Cards) use this method to retrieve their assigned value.
     *
     * @return The list of integer values for the shuffle.
     */
    public List<Integer> getValues() {
        return values;
    }
}