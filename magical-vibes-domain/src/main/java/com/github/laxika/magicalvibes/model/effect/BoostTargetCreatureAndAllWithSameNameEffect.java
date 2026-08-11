package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Gives target creature and every other creature with the same name +X/+Y until end of turn.
 * Only the named creature is targeted; the same-name creatures are affected without being
 * targeted.
 */
public record BoostTargetCreatureAndAllWithSameNameEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost
) implements CreatureBoostEffect {

    public BoostTargetCreatureAndAllWithSameNameEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
