package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * On resolution, the controller may pay {X}: the chosen creature gets +X/+0 until end of turn.
 */
public record PayXManaBoostTargetCreatureEffect() implements CreatureBoostEffect {

    /**
     * Squealing Devil's pay-X boost is a separate ability from its sacrifice clause, so an illegal
     * boost target must not stop the sacrifice from resolving. The handler skips the X payment when
     * the target is gone.
     */
    @Override
    public boolean resolvesWhenTargetIllegal() {
        return true;
    }

    @Override
    public DynamicAmount powerBoost() {
        return new XValue();
    }

    @Override
    public DynamicAmount toughnessBoost() {
        return new Fixed(0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
