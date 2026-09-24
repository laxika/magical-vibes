package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * SPELL-slot additional cast cost requiring the caster to sacrifice a rounded-up fraction of
 * matching permanents they control.
 */
public record SacrificeFractionRoundedUpCost(int divisor, PermanentPredicate filter)
        implements CostEffect {

    public SacrificeFractionRoundedUpCost {
        if (divisor <= 0) {
            throw new IllegalArgumentException("divisor must be positive");
        }
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }

    @Override
    public boolean sacrificesChosenPermanent() {
        return true;
    }
}
