package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals damage to any target, then perpetually grants a static effect if the target is a creature. */
public record DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffect(
        DynamicAmount damage, CardEffect staticEffect) implements DamageDealingEffect {

    public DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffect(
            int damage, CardEffect staticEffect) {
        this(new Fixed(damage), staticEffect);
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
