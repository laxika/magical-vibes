package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a replacement effect that doubles damage to the player damaged by this combat-damage
 * trigger and to permanents that player controls until the trigger controller's next turn.
 */
public record DoubleDamageToDamagedPlayerUntilNextTurnEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
