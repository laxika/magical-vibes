package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: schedule the combat opponent (a creature that becomes blocked by, or is blocking,
 * this permanent) so its controller gains control of it at end of combat, for as long as they
 * control the source. The Wretched-style "At end of combat, gain control of all creatures blocking
 * this creature for as long as you control this creature."
 * <p>
 * Placed on {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BECOMES_BLOCKED} with
 * {@link com.github.laxika.magicalvibes.model.TriggerMode#PER_BLOCKER} (one trigger per blocker). The
 * referenced creature is carried as the stack entry's target and the source is the entry's source
 * permanent; the trigger does not target (it can't fizzle). At resolution a delayed
 * {@link com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat} is queued;
 * control is applied with {@link ControlDuration#WHILE_SOURCE_ON_BATTLEFIELD} so it ends when the
 * source leaves the battlefield or its controller loses control of it.
 */
public record GainControlOfCombatOpponentAtEndOfCombatEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
