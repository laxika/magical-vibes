package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/** The creature enchanted by the source Aura deals the event amount of damage to each opponent. */
public record EnchantedCreatureDealsDamageToEachOpponentEffect(DynamicAmount amount)
        implements DamageDealingEffect {

    public EnchantedCreatureDealsDamageToEachOpponentEffect() {
        this(new EventValue());
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
