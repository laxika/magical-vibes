package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals {@code damage} to "it": the permanent the trigger event was about, the stack entry's triggering permanent,
 * referenced, not targeted, as it is when the ability resolves (nothing if it has left the battlefield). Which
 * permanents trigger the ability is the trigger's filter, judged as the event happens.
 *
 * <p>Raking Canopy ("Whenever a creature with flying attacks you, this enchantment deals 4 damage to it."), Aether
 * Flash ("Whenever a creature enters, this enchantment deals 2 damage to it.").
 */
public record DealDamageToTriggeringPermanentEffect(DynamicAmount damage) implements DamageDealingEffect {

    public DealDamageToTriggeringPermanentEffect(int damage) {
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
