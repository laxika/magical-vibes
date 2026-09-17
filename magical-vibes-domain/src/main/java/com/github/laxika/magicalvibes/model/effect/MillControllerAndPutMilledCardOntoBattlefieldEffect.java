package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Mills cards from the controller's library, then offers one matching milled card for the battlefield. */
public record MillControllerAndPutMilledCardOntoBattlefieldEffect(
        int count, CardPredicate filter, boolean mandatory)
        implements CardEffect {

    public MillControllerAndPutMilledCardOntoBattlefieldEffect(int count, CardPredicate filter) {
        this(count, filter, true);
    }

    public MillControllerAndPutMilledCardOntoBattlefieldEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }
}
