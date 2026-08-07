package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards all the cards in their hand, then draws that many cards.
 * All discards are automatic (no player choice). Fires discard triggers for each card.
 * Used by Collective Defiance (modal mode) as a targeted spell mode, and by Shocker on an
 * {@code ON_DAMAGE_TO_PLAYER} trigger, where the damaged player is baked onto the stack entry.
 */
public record TargetPlayerDiscardsHandThenDrawsThatManyEffect()
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
