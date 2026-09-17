package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to the player chosen by the source permanent as it entered the battlefield.
 * Used by Saskia the Unyielding for the damage dealt by the creature that caused the trigger.
 */
public record DealDamageToChosenPlayerEffect(DynamicAmount damage)
        implements DamageDealingEffect, CombatDamageDealerReferencingEffect {

    public DealDamageToChosenPlayerEffect(int damage) {
        this(new Fixed(damage));
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
