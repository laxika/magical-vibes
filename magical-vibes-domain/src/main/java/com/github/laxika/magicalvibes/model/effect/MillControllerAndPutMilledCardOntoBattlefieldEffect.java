package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Mills cards from the controller's library, then puts one matching milled card onto the battlefield. */
public record MillControllerAndPutMilledCardOntoBattlefieldEffect(int count, CardPredicate filter)
        implements CardEffect {

    public MillControllerAndPutMilledCardOntoBattlefieldEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }
}
