package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Gain control of every permanent matching {@code filter} that the target player controls,
 * permanently (Gilt-Leaf Archdruid's lands; Hellkite Tyrant's artifacts).
 *
 * <p>At resolution the controller gains control of every matching permanent that player controls at
 * that moment via the standard layer-2 control machinery (one permanent
 * {@link GainControlOfTargetEffect} floating effect per permanent). As a spell or activated ability
 * the player is chosen by targeting; as an {@code ON_COMBAT_DAMAGE_TO_PLAYER} trigger the damaged
 * player is bound as the stack entry's target instead.
 *
 * @param filter narrows which of the target player's permanents are seized
 */
public record GainControlOfAllPermanentsTargetPlayerControlsEffect(PermanentPredicate filter)
        implements CardEffect, CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
