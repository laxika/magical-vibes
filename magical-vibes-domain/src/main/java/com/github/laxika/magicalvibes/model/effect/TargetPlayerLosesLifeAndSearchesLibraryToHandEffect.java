package com.github.laxika.magicalvibes.model.effect;

/**
 * Draw-step triggered ability acting on the draw-step player (the stack entry's target): that player
 * loses {@code lifeLoss} life, then searches their library for a card, puts it into their hand, and
 * shuffles. The life loss and the search are both mandatory (the life loss isn't a payment). Used by
 * Maralen of the Mornsong in the {@code EACH_DRAW_TRIGGERED} slot alongside a static
 * {@link PlayersCannotDrawCardsEffect}.
 */
public record TargetPlayerLosesLifeAndSearchesLibraryToHandEffect(int lifeLoss) implements CardEffect {
}
