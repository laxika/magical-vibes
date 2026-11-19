package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the number of +1/+1 counters on the permanents {@code subject} names (CR 701.10e: each gets as many +1/+1
 * counters as it already has): by default each target creature ("double the number of +1/+1 counters on target
 * creature"), or a permanent named without targeting it: {@link Subjects#source()} ("on this creature"),
 * {@link Subjects#triggering()} ("on it", the creature the trigger event was about: "whenever equipped creature
 * attacks, double the number of +1/+1 counters on it") or {@link Subjects#attached()} ("on enchanted creature").
 *
 * <p>Only {@code target()} targets; any other subject declares no target, so shroud and hexproof don't stop it.
 */
public record DoublePlusOneCountersOnTargetCreatureEffect(PermanentSubject subject)
        implements CardEffect, DoublingEffect {

    public DoublePlusOneCountersOnTargetCreatureEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
    }

    /** "Double the number of +1/+1 counters on target creature." */
    public DoublePlusOneCountersOnTargetCreatureEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
