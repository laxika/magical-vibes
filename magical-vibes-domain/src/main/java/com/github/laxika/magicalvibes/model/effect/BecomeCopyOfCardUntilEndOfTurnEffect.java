package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Causes the source permanent to become a copy of the supplied creature card until end of turn.
 */
public record BecomeCopyOfCardUntilEndOfTurnEffect(Card card) implements CardEffect {
}
