/**
 * Matching Pairs Game - Card State Definition
 * Version: 1.0
 * Author: Giuseppe Muschetta
 */
import java.awt.Color;

/**
 * Defines the possible states of a game Card, each associated with a specific color
 * for visual representation. This enumeration provides a type-safe way to manage
 * card states throughout the game logic.
 */
public enum CardState {
    /**
     * The state where the card's value is hidden from the player.
     * Represented by the color Green.
     */
    FACE_DOWN(Color.GREEN),

    /**
     * The state where the card's value is visible to the player.
     * Represented by the color White.
     */
    FACE_UP(Color.WHITE),

    /**
     * The state where the card has been successfully matched and removed
     * from active play. Represented by the color Red.
     */
    EXCLUDED(Color.RED);

    // The color associated with this card state.
    private final Color color;

    /**
     * Constructs a CardState enum constant with its associated color.
     * @param color The java.awt.Color instance for this state.
     */
    CardState(Color color) {
        this.color = color;
    }

    /**
     * Retrieves the color associated with this card state.
     * Used for updating the card's visual appearance.
     * @return The Color object representing this state.
     */
    public Color getColor() {
        return color;
    }
}