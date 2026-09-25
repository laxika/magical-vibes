package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Perpetually reduces the generic cast cost of matching cards currently in the controller's hand. */
public record PerpetuallyReduceCostForMatchingHandCardsEffect(CardPredicate filter, int amount)
        implements CardEffect {

    public PerpetuallyReduceCostForMatchingHandCardsEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
