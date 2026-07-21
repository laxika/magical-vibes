package com.github.laxika.magicalvibes.model.effect;

/**
 * Where a card looked at (or revealed) from the top of a library is put once the choice resolves.
 * Used by {@link LookAtTopCardsEffect}'s {@code restDestination} for the not-chosen cards.
 *
 * <p>{@code restDestination} is always {@code GRAVEYARD}, {@code BOTTOM_OF_LIBRARY},
 * {@code BOTTOM_OF_LIBRARY_RANDOM}, or {@code EXILE}; {@code HAND} is unused (the chosen cards'
 * destination is the separate {@code chosenDestination} axis, a {@code LibrarySearchDestination}).
 */
public enum LookDestination {
    HAND,
    GRAVEYARD,
    BOTTOM_OF_LIBRARY,
    /** Rest go to the bottom in a random order (no player reorder) — Memory Deluge. */
    BOTTOM_OF_LIBRARY_RANDOM,
    EXILE
}
