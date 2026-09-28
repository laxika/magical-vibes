package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Perpetually modifies the power and toughness of a target creature's runtime card. */
public record PerpetuallyBoostTargetCreatureEffect(int powerBoost, int toughnessBoost,
                                                   PermanentPredicate filter)
        implements CardEffect {

    public PerpetuallyBoostTargetCreatureEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, new PermanentControlledBySourceControllerPredicate());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter);
    }
}
