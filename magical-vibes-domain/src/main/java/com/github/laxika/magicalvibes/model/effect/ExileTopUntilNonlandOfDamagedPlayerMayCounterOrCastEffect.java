package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the combat-damaged player's library until a nonland card is found, then lets
 * the source controller choose a +1/+1 counter on the source or a temporary normal-cost cast of
 * the exiled nonland card using mana of any type.
 */
public record ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
