package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until a nonland card is revealed, then
 * puts all cards revealed this way into the controller's hand. If the library is exhausted first,
 * all revealed cards are still put into the controller's hand.
 * <p>
 * Used by Treasure Hunt.
 */
public record RevealUntilNonlandCardsToHandEffect() implements CardEffect {
}
