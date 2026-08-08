package com.github.laxika.magicalvibes.model.amount;

/**
 * The mana value of the most recently discarded card, or 0 when nothing has been discarded yet.
 * Recorded by the central discard hook, so it reads the card just discarded by an earlier effect
 * of the same spell: "Draw three cards, then discard a card. [This spell] deals damage equal to the
 * discarded card's mana value to that permanent or player" (Blast of Genius).
 */
public record LastDiscardedCardManaValue() implements DynamicAmount {
}
