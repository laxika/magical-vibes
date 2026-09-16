package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Chooses an opponent at random, then deals damage to that player or a planeswalker they control.
 * The choice is not a target, matching the rules wording of Vial Smasher the Fierce.
 */
public record DealDamageToRandomOpponentOrTheirPlaneswalkerEffect(DynamicAmount damage)
        implements DamageDealingEffect, TriggeringSpellManaValueEffect {

    public DealDamageToRandomOpponentOrTheirPlaneswalkerEffect(int damage) {
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
