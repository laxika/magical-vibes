package com.github.laxika.magicalvibes.model.effect;

/** How long a {@link GrantNoMaximumHandSizeEffect} grant lasts. */
public enum NoMaximumHandSizeDuration {
    /** For the rest of the game (Praetor's Counsel, Tamiyo's and Wrenn's emblems). */
    REST_OF_GAME,
    /** Until the beginning of the controller's next turn (Enter the Infinite). */
    UNTIL_NEXT_TURN,
    /** For as long as the source permanent stays on the battlefield under its controller's control (The Great Synthesis). */
    WHILE_SOURCE_ON_BATTLEFIELD
}
