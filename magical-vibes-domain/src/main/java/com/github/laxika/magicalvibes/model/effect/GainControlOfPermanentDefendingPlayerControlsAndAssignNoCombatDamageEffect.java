package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "You may gain control of target [filter] defending player controls[ for as long as you control
 * this creature]. If you do, this creature assigns no combat damage this turn." (Orcish Squatters
 * — lands, {@link ControlDuration#WHILE_SOURCE_ON_BATTLEFIELD}; Kukemssa Pirates — artifacts,
 * {@link ControlDuration#PERMANENT}.)
 *
 * <p>Wrap in a {@link MayEffect} on an {@code ON_ATTACKS_UNBLOCKED} trigger. The stack entry's
 * {@code targetId} is the defending player and {@code sourcePermanentId} the attacking creature.
 * On resolution the controller picks one permanent the defending player controls that matches
 * {@code filter}; control of it is taken for {@code duration} (a wrapped
 * {@link GainControlOfTargetEffect}), and — only when a permanent is actually taken — the source
 * assigns no combat damage this turn.
 *
 * @param filter which of the defending player's permanents may be chosen
 * @param duration how long control is kept
 * @param choiceNoun singular noun used in the choice prompt and log (e.g. {@code "land"})
 */
public record GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect(
        PermanentPredicate filter, ControlDuration duration, String choiceNoun)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }
}
