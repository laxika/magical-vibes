package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Perpetually reduces the generic cast cost of matching cards owned by the effect controller. */
public record PerpetuallyReduceCostForMatchingOwnedCardsEffect(CardPredicate filter, int amount)
        implements CardEffect {

    public PerpetuallyReduceCostForMatchingOwnedCardsEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
