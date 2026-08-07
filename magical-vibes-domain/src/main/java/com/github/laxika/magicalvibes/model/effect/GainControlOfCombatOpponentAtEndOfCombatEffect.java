package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: schedule the combat opponent (a creature that becomes blocked by, or is blocking,
 * this permanent) so its controller gains control of it at end of combat, for {@code duration}.
 * The Wretched-style "At end of combat, gain control of all creatures blocking this creature for as
 * long as you control this creature" ({@link ControlDuration#WHILE_SOURCE_ON_BATTLEFIELD}, the
 * no-arg default), or Tolarian Entrancer's "Whenever this creature becomes blocked by a creature,
 * gain control of that creature at end of combat" ({@link ControlDuration#PERMANENT}).
 * <p>
 * Placed on {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BECOMES_BLOCKED} with
 * {@link com.github.laxika.magicalvibes.model.TriggerMode#PER_BLOCKER} (one trigger per blocker). The
 * referenced creature is carried as the stack entry's target and the source is the entry's source
 * permanent; the trigger does not target (it can't fizzle). At resolution a delayed
 * {@link com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat} is queued
 * carrying the duration. A source-linked duration additionally requires the source to still be on
 * the battlefield under the same controller at end of combat; a {@code PERMANENT} gain happens even
 * if the source has left.
 */
public record GainControlOfCombatOpponentAtEndOfCombatEffect(ControlDuration duration)
        implements ControlStealingEffect {

    public GainControlOfCombatOpponentAtEndOfCombatEffect() {
        this(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD);
    }

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
