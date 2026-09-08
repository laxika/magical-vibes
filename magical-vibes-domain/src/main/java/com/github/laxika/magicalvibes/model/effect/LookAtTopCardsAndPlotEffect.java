package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Looks at the top cards of a library, optionally plots one matching card, and puts the rest into hand. */
public record LookAtTopCardsAndPlotEffect(int count, CardPredicate filter) implements CardEffect {
}
