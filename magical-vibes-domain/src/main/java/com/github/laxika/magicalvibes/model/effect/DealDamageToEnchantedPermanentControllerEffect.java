package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals non-targeted damage to the current controller of the permanent attached to the source Aura. */
public record DealDamageToEnchantedPermanentControllerEffect(DynamicAmount amount)
        implements DamageDealingEffect {

    public DealDamageToEnchantedPermanentControllerEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
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
