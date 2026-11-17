package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: the controller gains control of the combat opponent (a creature that becomes
 * blocked by, or is blocking, this permanent) at end of combat, for {@code duration}. The
 * Wretched-style "for as long as you control this creature"
 * ({@link Duration#WHILE_SOURCE_ON_BATTLEFIELD}, the no-arg default), or Tolarian Entrancer's
 * "Whenever this creature becomes blocked by a creature, gain control of that creature at end of
 * combat" ({@link Duration#PERMANENT}).
 * <p>
 * Placed on {@code Triggers.becomesBlocked()} ability with
 * {@code TriggerOptions.EACH_BLOCKER} (one trigger per blocker). The
 * referenced creature is carried as the stack entry's target and the source is the entry's source
 * permanent; the trigger does not target (it can't fizzle). Resolution registers a delayed "at end of combat"
 * ability about the creature carrying a {@link GainControlOfTargetEffect} with the duration, so the control change
 * uses the stack.
 */
public record GainControlOfCombatOpponentAtEndOfCombatEffect(Duration duration)
        implements ControlStealingEffect {

    public GainControlOfCombatOpponentAtEndOfCombatEffect() {
        this(Duration.WHILE_SOURCE_ON_BATTLEFIELD);
    }

    @Override
    public Duration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
