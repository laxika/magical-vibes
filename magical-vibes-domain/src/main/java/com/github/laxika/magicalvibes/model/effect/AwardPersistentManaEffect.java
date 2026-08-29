package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Adds mana that remains in the recipient's mana pool through step and phase transitions until
 * the end of the turn. This is a triggered effect rather than a mana ability.
 */
public record AwardPersistentManaEffect(ManaColor color, DynamicAmount amount, Recipient recipient)
        implements CombatDamageAmountAwareEffect, CombatDamageTriggerContextEffect {

    public AwardPersistentManaEffect(ManaColor color, DynamicAmount amount) {
        this(color, amount, Recipient.CONTROLLER);
    }

    public enum Recipient {
        CONTROLLER,
        TARGET_PLAYER,
        ENCHANTED_PERMANENT_CONTROLLER
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return amount;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return recipient == Recipient.ENCHANTED_PERMANENT_CONTROLLER
                ? TriggerContext.ENCHANTED_CREATURE_CONTROLLER
                : null;
    }
}
