package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost: pay a fixed amount of life or sacrifice one matching permanent.
 * Exactly one option is paid; a null permanent selection pays the life option.
 */
public record PayLifeOrSacrificePermanentCost(int lifeAmount, PermanentPredicate filter)
        implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }

    @Override
    public int lifePaid(int currentLife) {
        return lifeAmount;
    }
}
