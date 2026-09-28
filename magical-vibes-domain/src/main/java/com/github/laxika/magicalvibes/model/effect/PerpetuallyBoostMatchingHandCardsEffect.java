package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Perpetually gives a power and toughness bonus to every matching card currently in hand. */
public record PerpetuallyBoostMatchingHandCardsEffect(
        CardPredicate filter, int power, int toughness) implements CardEffect {
}
