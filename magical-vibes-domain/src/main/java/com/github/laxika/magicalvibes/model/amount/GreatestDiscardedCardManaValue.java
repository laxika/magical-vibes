package com.github.laxika.magicalvibes.model.amount;

/**
 * The greatest mana value among cards discarded by the current discard effect, or 0 when no card
 * was discarded.
 */
public record GreatestDiscardedCardManaValue() implements DynamicAmount {
}
