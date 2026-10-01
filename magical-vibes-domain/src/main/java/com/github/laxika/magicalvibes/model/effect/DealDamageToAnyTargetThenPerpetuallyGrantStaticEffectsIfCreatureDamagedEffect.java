package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/**
 * Deals damage to any target, then perpetually grants static effects to each creature actually
 * dealt damage by this effect.
 */
public record DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffect(
        DynamicAmount damage,
        List<CardEffect> staticEffects) implements DamageDealingEffect {

    public DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffect(
            int damage, List<CardEffect> staticEffects) {
        this(new Fixed(damage), staticEffects);
    }

    public DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffect {
        staticEffects = List.copyOf(staticEffects);
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
