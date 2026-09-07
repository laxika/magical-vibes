package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Records whether the most recently resolved discard effect discarded a card of the given type.
 * The result is stored as {@code 1} or {@code 0} on the resolving stack entry for a following
 * {@link EventValueAtLeast} condition or other event-value reader.
 */
public record RecordLastDiscardedCardTypeEffect(CardType cardType) implements CardEffect {
}
