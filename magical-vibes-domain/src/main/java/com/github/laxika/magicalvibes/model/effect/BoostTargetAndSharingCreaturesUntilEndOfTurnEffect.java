package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Gives the target creature and every other creature sharing a color with it +X/+Y until end of
 * turn. The affected creatures are determined on resolution.
 */
public record BoostTargetAndSharingCreaturesUntilEndOfTurnEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost
) implements CreatureBoostEffect {

    public BoostTargetAndSharingCreaturesUntilEndOfTurnEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
