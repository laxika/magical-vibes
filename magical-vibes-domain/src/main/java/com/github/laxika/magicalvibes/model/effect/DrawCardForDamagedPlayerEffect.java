package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Draws cards for the player dealt combat damage by the triggering creature. */
public record DrawCardForDamagedPlayerEffect(DynamicAmount amount)
        implements CardDrawingEffect, CombatDamageTriggerContextEffect {

    public DrawCardForDamagedPlayerEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
