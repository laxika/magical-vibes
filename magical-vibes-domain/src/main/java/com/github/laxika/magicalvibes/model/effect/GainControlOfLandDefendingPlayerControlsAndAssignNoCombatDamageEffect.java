package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may gain control of target land defending player controls for as long as you control this
 * creature. If you do, this creature assigns no combat damage this turn." (Orcish Squatters.)
 *
 * <p>Wrap in a {@link MayEffect} on an {@code ON_ATTACKS_UNBLOCKED} trigger. The stack entry's
 * {@code targetId} is the defending player and {@code sourcePermanentId} the attacking creature.
 * On resolution the controller picks one land the defending player controls; control of it is taken
 * for as long as the source stays on the battlefield (a wrapped
 * {@link GainControlOfTargetEffect} with {@link ControlDuration#WHILE_SOURCE_ON_BATTLEFIELD}), and
 * — only when a land is actually taken — the source assigns no combat damage this turn.
 */
public record GainControlOfLandDefendingPlayerControlsAndAssignNoCombatDamageEffect()
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD;
    }
}
