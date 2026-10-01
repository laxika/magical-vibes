package com.github.laxika.magicalvibes.model.effect;

/**
 * Perpetually gives a random nonland card in the combat-damaged player's hand a trigger that
 * gives its caster a poison counter when cast.
 */
public record PerpetuallyGivePoisonCounterOnCastToRandomNonlandCardInDamagedHandEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
