package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals damage to the owner of target haunted creature. */
public record DealDamageToOwnerOfTargetHauntedCreatureEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToOwnerOfTargetHauntedCreatureEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
