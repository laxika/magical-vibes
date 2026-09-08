package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/** Reduces the generic activation cost of the controller's boast abilities dynamically. */
public record ReduceBoastActivationCostEffect(DynamicAmount amount)
        implements ActivatedAbilityCostReducingEffect {

    private static final PermanentPredicate ALL_PERMANENTS = new PermanentTruePredicate();

    @Override
    public PermanentPredicate affectedPermanents() {
        return ALL_PERMANENTS;
    }

    @Override
    public DynamicAmount genericCostReductionAmount() {
        return amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return ability.isBoast();
    }

    @Override
    public boolean appliesSymmetrically() {
        return false;
    }
}
