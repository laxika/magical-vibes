package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards a card at random. If they can't (empty hand), the controller draws a card instead.
 */
public record TargetPlayerRandomDiscardOrControllerDrawsEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
