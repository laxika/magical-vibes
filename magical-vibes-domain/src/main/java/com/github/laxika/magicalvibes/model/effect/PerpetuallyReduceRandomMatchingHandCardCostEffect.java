package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Perpetually reduces the generic cast cost of one randomly selected matching hand card. */
public record PerpetuallyReduceRandomMatchingHandCardCostEffect(CardPredicate filter, int amount)
        implements CardEffect {

    public PerpetuallyReduceRandomMatchingHandCardCostEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
