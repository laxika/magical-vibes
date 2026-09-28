package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each targeted player loses life and sacrifices a creature of their choice.
 * The sacrifice choices are made in APNAP order. The filter and ordering flags
 * support variants that restrict the sacrifice to a permanent characteristic
 * or apply the sacrifice before the life loss.
 */
public record EachTargetPlayerLosesLifeAndSacrificesCreatureEffect(
        int lifeLoss,
        PermanentPredicate sacrificeFilter,
        boolean sacrificeBeforeLifeLoss
) implements CardEffect {

    public EachTargetPlayerLosesLifeAndSacrificesCreatureEffect(int lifeLoss) {
        this(lifeLoss, new PermanentIsCreaturePredicate(), false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
