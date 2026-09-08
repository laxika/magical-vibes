package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles one matching card from the controller's hand with refine counters on it. */
public record ExileCardFromHandWithRefineCountersEffect(CardPredicate filter, int counterCount,
                                                         String description) implements CardEffect {

    public ExileCardFromHandWithRefineCountersEffect {
        if (counterCount < 1) {
            throw new IllegalArgumentException("counterCount must be positive");
        }
    }
}
