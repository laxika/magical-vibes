package com.github.laxika.magicalvibes.model.effect;

/**
 * Where a card looked at (or revealed) from the top of a library is put once the choice resolves.
 * Used by {@link LookAtTopCardsEffect}'s {@code restDestination} for the not-chosen cards.
 *
 * <p>{@code HAND} is used when a revealed top card that wasn't cast is put into its controller's
 * hand. Other destinations are {@code GRAVEYARD}, {@code BOTTOM_OF_LIBRARY},
 * {@code BOTTOM_OF_LIBRARY_RANDOM}, {@code TOP_OF_LIBRARY}, {@code EXILE}, and
 * {@code SHUFFLE_INTO_LIBRARY}.
 */
public enum LookDestination {
    HAND,
    GRAVEYARD,
    BOTTOM_OF_LIBRARY,
    /** Rest go back on top of the library in an order the player chooses — Diabolic Vision. */
    TOP_OF_LIBRARY,
    /** Rest go to the bottom in a random order (no player reorder) — Memory Deluge. */
    BOTTOM_OF_LIBRARY_RANDOM,
    EXILE,
    /** Rest are shuffled into the library. */
    SHUFFLE_INTO_LIBRARY
}
