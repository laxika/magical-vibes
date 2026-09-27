package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

/** Perpetually modifies the power and toughness of a target creature's runtime card. */
public record PerpetuallyBoostTargetCreatureEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentControlledBySourceControllerPredicate());
    }
}
