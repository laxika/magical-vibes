package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Additional cost to cast a spell: return a matching permanent you control to its owner's hand.
 * The permanent is supplied via {@code PlayCardRequest.sacrificePermanentId} and returned
 * before mana is paid.
 */
public record ReturnPermanentToHandCost(PermanentPredicate filter) implements CostEffect {

    private static final PermanentPredicate PERMANENT_FILTER = new PermanentTruePredicate();

    public ReturnPermanentToHandCost() {
        this(PERMANENT_FILTER);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
