package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to the target creature and each other creature sharing a creature type with it.
 * The affected creatures are determined on resolution.
 */
public record DealDamageToTargetAndCreaturesSharingCreatureTypeEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToTargetAndCreaturesSharingCreatureTypeEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
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
        return false;
    }
}
