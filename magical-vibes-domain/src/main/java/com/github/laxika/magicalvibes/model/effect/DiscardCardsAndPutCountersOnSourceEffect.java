package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller discard any number of matching cards, then puts a fixed number of +1/+1
 * counters on the source for each card discarded.
 */
public record DiscardCardsAndPutCountersOnSourceEffect(
        CardPredicate cardFilter,
        int countersPerCard,
        String cardDescription
) implements CardEffect {

    public DiscardCardsAndPutCountersOnSourceEffect {
        if (countersPerCard < 0) {
            throw new IllegalArgumentException("countersPerCard must not be negative");
        }
    }
}
