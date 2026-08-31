package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals the amount stored as the stack entry's event value to each creature and planeswalker the
 * damaged player controls.
 */
public record DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
