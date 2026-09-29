package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Activated-ability cost for blighting a creature for the ability's announced X value.
 *
 * @param maximumX optional dynamic upper bound for X, evaluated while the ability is activated
 */
public record BlightXCost(DynamicAmount maximumX) implements CostEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    public BlightXCost() {
        this(null);
    }

    public CounterType counterType() {
        return CounterType.MINUS_ONE_MINUS_ONE;
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }
}
