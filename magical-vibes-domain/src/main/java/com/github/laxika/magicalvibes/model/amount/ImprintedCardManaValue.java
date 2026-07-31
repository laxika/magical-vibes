package com.github.laxika.magicalvibes.model.amount;

/**
 * The mana value of the card imprinted on the source permanent, or 0 when nothing is imprinted.
 * Pair with {@code ExileTopCardOfLibraryCost(n, true)} for "Exile the top card of your library:
 * ... where X is the exiled card's mana value" (Phyrexian Devourer).
 */
public record ImprintedCardManaValue() implements DynamicAmount {
}
