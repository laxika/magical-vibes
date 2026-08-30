package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Lets the controller choose a matching card from hand, exile it, and plot it. */
public record ExileCardFromHandAndPlotEffect(CardPredicate filter, String description) implements CardEffect {
}
