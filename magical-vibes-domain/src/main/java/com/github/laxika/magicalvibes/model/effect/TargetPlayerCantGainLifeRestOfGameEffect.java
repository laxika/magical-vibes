package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeting ability effect: the player carried as the stack entry's target
 * (e.g. the damaged player of an {@code ON_DAMAGE_TO_PLAYER} trigger) can't gain
 * life for the rest of the game.
 * Used by Stigma Lasher.
 */
public record TargetPlayerCantGainLifeRestOfGameEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
