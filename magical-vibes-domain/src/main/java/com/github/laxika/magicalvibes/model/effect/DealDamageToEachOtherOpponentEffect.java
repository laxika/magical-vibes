package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals the combat-damage event amount to each opponent other than the damaged opponent. */
public record DealDamageToEachOtherOpponentEffect(DynamicAmount amount)
        implements DamageDealingEffect, CombatDamageAmountAwareEffect, CombatDamageTriggerContextEffect {

    public DealDamageToEachOtherOpponentEffect() {
        this(new EventValue());
    }

    public DealDamageToEachOtherOpponentEffect(int amount) {
        this(new Fixed(amount));
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
