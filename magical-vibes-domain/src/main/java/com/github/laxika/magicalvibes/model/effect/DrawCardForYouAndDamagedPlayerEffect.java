package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "You and that player each draw {@code amount} cards" for a combat-damage trigger: the ability's
 * controller and the player dealt combat damage by the triggering creature draw in turn order
 * starting with the active player (CR 608.2f), which need not be the controller.
 */
public record DrawCardForYouAndDamagedPlayerEffect(DynamicAmount amount)
        implements CardDrawingEffect, CombatDamageTriggerContextEffect {

    public DrawCardForYouAndDamagedPlayerEffect(int amount) {
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
