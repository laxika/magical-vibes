package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/** Exiles a random card with the combat-damage amount as its mana value from the damaged player's library. */
public record ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffect()
        implements CombatDamageTriggerContextEffect, CombatDamageAmountAwareEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return new EventValue();
    }
}
