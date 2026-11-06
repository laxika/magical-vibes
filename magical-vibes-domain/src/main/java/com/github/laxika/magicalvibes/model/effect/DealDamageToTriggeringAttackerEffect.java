package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals {@code damage} to the attacking creature that caused this attack trigger: the stack entry's triggering
 * permanent, referenced, not targeted. Which attackers trigger the ability is the trigger's filter
 * ({@code Triggers.creatureAttacksYou().withFilter(...)}), judged as the creature is declared an attacker.
 *
 * <p>Used by Raking Canopy ("Whenever a creature with flying attacks you, this enchantment deals 4 damage to it.").
 */
public record DealDamageToTriggeringAttackerEffect(DynamicAmount damage) implements DamageDealingEffect {

    public DealDamageToTriggeringAttackerEffect(int damage) {
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
