package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Gain control of every permanent matching {@code filter} that the target player controls,
 * for the requested duration (Gilt-Leaf Archdruid's lands; Hellkite Tyrant's artifacts).
 *
 * <p>At resolution the controller gains control of every matching permanent that player controls at
 * that moment via the standard layer-2 control machinery (one permanent
 * {@link GainControlOfTargetEffect} floating effect per permanent). As a spell or activated ability
 * the player is chosen by targeting; as an {@code ON_COMBAT_DAMAGE_TO_PLAYER} trigger the damaged
 * player is bound as the stack entry's target instead.

 * <p>Optional {@code thenEffects} resolve for every permanent gained after all control changes have
 * been applied. They are useful for riders such as untapping, granting haste, and granting
 * temporary static abilities to exactly the permanents seized by the effect.
 *
 * @param filter     narrows which of the target player's permanents are seized
 * @param duration   how long the control gain lasts
 * @param thenEffects ordered rider effects applied to each seized permanent
 */
public record GainControlOfAllPermanentsTargetPlayerControlsEffect(
        PermanentPredicate filter, ControlDuration duration, List<CardEffect> thenEffects)
        implements CardEffect, CombatDamageTriggerContextEffect {

    public GainControlOfAllPermanentsTargetPlayerControlsEffect {
        thenEffects = List.copyOf(thenEffects);
    }

    public GainControlOfAllPermanentsTargetPlayerControlsEffect(PermanentPredicate filter) {
        this(filter, ControlDuration.PERMANENT, List.of());
    }

    public GainControlOfAllPermanentsTargetPlayerControlsEffect(PermanentPredicate filter,
                                                                 ControlDuration duration) {
        this(filter, duration, List.of());
    }

    public GainControlOfAllPermanentsTargetPlayerControlsEffect(PermanentPredicate filter,
                                                                 ControlDuration duration,
                                                                 CardEffect... thenEffects) {
        this(filter, duration, List.of(thenEffects));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
