package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that adds loyalty counters to the cost of matching loyalty abilities controlled
 * by the static effect's controller (Carth the Lion).
 */
public record IncreaseLoyaltyAbilityCostEffect(PermanentPredicate predicate, int amount)
        implements LoyaltyAbilityCostIncreasingEffect {

    @Override
    public PermanentPredicate affectedPermanents() {
        return predicate;
    }

    @Override
    public int additionalLoyaltyCost() {
        return amount;
    }
}
