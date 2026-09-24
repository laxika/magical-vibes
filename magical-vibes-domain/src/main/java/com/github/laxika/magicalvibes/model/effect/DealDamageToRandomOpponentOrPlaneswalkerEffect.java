package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Chooses an opponent at random, then lets the effect controller choose that opponent or a
 * planeswalker they control for the damage. The choice is made as the effect resolves, so this
 * effect does not target.
 *
 * @param damage the amount of damage to deal, evaluated at resolution
 */
public record DealDamageToRandomOpponentOrPlaneswalkerEffect(DynamicAmount damage)
        implements DamageDealingEffect, TriggeringSpellManaValueEffect {

    public DealDamageToRandomOpponentOrPlaneswalkerEffect(int damage) {
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
