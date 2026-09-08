package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller discard any number of matching cards, then puts a fixed number of +1/+1
 * counters on the source and draws a fixed number of cards for each card discarded.
 */
public record DiscardCardsAndPutCountersOnSourceEffect(
        CardPredicate cardFilter,
        int countersPerCard,
        int drawCardsPerCard,
        String cardDescription
) implements CardEffect {

    public DiscardCardsAndPutCountersOnSourceEffect(CardPredicate cardFilter, int countersPerCard,
                                                     String cardDescription) {
        this(cardFilter, countersPerCard, 0, cardDescription);
    }

    public DiscardCardsAndPutCountersOnSourceEffect {
        if (countersPerCard < 0) {
            throw new IllegalArgumentException("countersPerCard must not be negative");
        }
        if (drawCardsPerCard < 0) {
            throw new IllegalArgumentException("drawCardsPerCard must not be negative");
        }
    }
}
