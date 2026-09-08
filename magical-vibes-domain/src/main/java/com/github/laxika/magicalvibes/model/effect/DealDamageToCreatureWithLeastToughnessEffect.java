package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to the creature with the least toughness among all creatures on the battlefield.
 * If multiple creatures are tied, the effect's controller chooses one when the effect resolves.
 */
public record DealDamageToCreatureWithLeastToughnessEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToCreatureWithLeastToughnessEffect(int damage) {
        this(new Fixed(damage));
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
