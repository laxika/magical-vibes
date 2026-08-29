package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches the controller's outside-the-game card pool for a matching card to put into hand. */
public record SearchOutsideGameForCardToHandEffect(CardPredicate filter) implements CardEffect {
}
