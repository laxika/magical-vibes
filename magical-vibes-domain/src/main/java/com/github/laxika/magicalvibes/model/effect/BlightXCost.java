package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Activated-ability cost for blighting a creature for the ability's announced X value, or for a
 * fixed amount when {@code fixedAmount} is set (e.g. Gristle Glutton's "Blight 1").
 *
 * @param maximumX optional dynamic upper bound for X, evaluated while the ability is activated
 * @param fixedAmount optional fixed number of -1/-1 counters, used instead of the announced X
 */
public record BlightXCost(DynamicAmount maximumX, Integer fixedAmount) implements CostEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    public BlightXCost(DynamicAmount maximumX) {
        this(maximumX, null);
    }

    public BlightXCost() {
        this(null, null);
    }

    public static BlightXCost fixed(int amount) {
        return new BlightXCost(null, amount);
    }

    public CounterType counterType() {
        return CounterType.MINUS_ONE_MINUS_ONE;
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }
}
