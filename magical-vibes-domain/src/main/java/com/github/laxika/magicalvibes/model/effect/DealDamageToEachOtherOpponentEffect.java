package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals the combat-damage event amount to each opponent other than the damaged opponent.
 *
 * @param dealerIsSource whether the creature that dealt the combat damage is the damage source
 *                       ("it deals that much damage"); otherwise the trigger's source deals it
 */
public record DealDamageToEachOtherOpponentEffect(DynamicAmount amount, boolean dealerIsSource)
        implements DamageDealingEffect, CombatDamageAmountAwareEffect, CombatDamageTriggerContextEffect,
        CombatDamageDealerReferencingEffect {

    public DealDamageToEachOtherOpponentEffect() {
        this(new EventValue(), true);
    }

    public DealDamageToEachOtherOpponentEffect(int amount) {
        this(new Fixed(amount), false);
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

    @Override
    public DynamicAmount combatDamageAmount() {
        return amount;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
