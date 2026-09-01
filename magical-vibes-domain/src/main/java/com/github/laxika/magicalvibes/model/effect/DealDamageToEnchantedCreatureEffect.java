package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals non-targeted damage to the creature enchanted by the source Aura. */
public record DealDamageToEnchantedCreatureEffect(DynamicAmount amount)
        implements DamageDealingEffect {

    public DealDamageToEnchantedCreatureEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
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
