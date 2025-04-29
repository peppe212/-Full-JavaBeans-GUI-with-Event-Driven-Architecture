/**
 * Matching Pairs Game - Shuffle Listener Interface
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import java.util.EventListener;

/**
 * Defines the interface for objects that need to be notified when a
 * shuffle event occurs. This follows the standard Java event listener pattern
 * and is part of the Observer design pattern implementation. Components
 * interested in receiving new card values after a shuffle should implement
 * this interface.
 */
public interface ShuffleListener extends EventListener {
    /**
     * Invoked when a shuffle event is fired by the event source (Board).
     * Implementers should contain the logic to handle the shuffle, typically
     * updating their state based on the information within the event object.
     *
     * @param evt The ShuffleEvent object containing details about the shuffle,
     * including the new sequence of card values.
     */
    void shufflePerformed(ShuffleEvent evt);
}