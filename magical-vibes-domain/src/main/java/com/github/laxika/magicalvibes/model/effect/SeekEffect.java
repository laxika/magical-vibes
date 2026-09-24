package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Randomly seeks one matching card from the controller's library into their hand. */
public record SeekEffect(CardPredicate filter) implements CardEffect {
}
