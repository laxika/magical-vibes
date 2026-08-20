package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals damage to any target, then boosts each creature actually dealt damage by this effect. */
public record DealDamageToAnyTargetThenBoostIfCreatureDamagedEffect(
        DynamicAmount damage,
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost)
        implements DamageDealingEffect, CreatureBoostEffect {

    public DealDamageToAnyTargetThenBoostIfCreatureDamagedEffect(int damage, int powerBoost,
                                                                  int toughnessBoost) {
        this(new Fixed(damage), new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
