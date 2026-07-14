package com.github.laxika.magicalvibes.model.effect;

/**
 * Where a card looked at (or revealed) from the top of a library is put once the choice resolves.
 * Used by {@link LookAtTopCardsEffect}'s {@code restDestination} for the not-chosen cards.
 *
 * <p>{@code HAND} is reserved for a future {@code chosenDestination} generalization (battlefield /
 * graveyard "choose to hand vs elsewhere" look effects are not yet folded in — see the roadmap);
 * {@code restDestination} today is always {@code GRAVEYARD}, {@code BOTTOM_OF_LIBRARY}, or
 * {@code EXILE}.
 */
public enum LookDestination {
    HAND,
    GRAVEYARD,
    BOTTOM_OF_LIBRARY,
    EXILE
}
