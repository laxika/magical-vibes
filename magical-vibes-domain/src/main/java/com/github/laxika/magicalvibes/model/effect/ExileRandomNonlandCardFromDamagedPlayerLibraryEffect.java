package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Exiles a random nonland card from the damaged player's library and tracks it with the source.
 * The source's controller may play the exiled card, spending mana as though it were any color,
 * while {@code permissionCondition} holds; the permission outlives the source leaving the battlefield.
 *
 * @param permissionCondition condition under which the controller may play the exiled card
 */
public record ExileRandomNonlandCardFromDamagedPlayerLibraryEffect(Condition permissionCondition)
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
