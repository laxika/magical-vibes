package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player discards a card at random, then draws a card if a card was discarded.
 *
 * <p>The target is supplied by the combat-damage trigger context when used on
 * {@code ON_COMBAT_DAMAGE_TO_PLAYER}.
 */
public record TargetPlayerRandomDiscardThenDrawEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
