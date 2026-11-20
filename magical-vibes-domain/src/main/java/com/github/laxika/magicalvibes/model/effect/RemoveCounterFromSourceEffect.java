package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Remove up to {@code amount} counters of the given type from the {@code subject} permanent, clamped at zero (no-op if
 * it has none). A dynamic amount is evaluated on resolution, against the permanent the counters come off. The
 * player-targeting form uses the stack entry's target player for that amount while still removing counters from the
 * source permanent. Used by Shrewd Hatchling and Descendant of Masumaro.
 *
 * <p>{@code subject} is {@link Subjects#source()} by default ("remove a counter from this creature"). It can name
 * another permanent without targeting it: {@link Subjects#attachedAtTrigger()} for Venarian Gold's "remove a sleep
 * counter from that creature", {@link Subjects#attached()} for "enchanted creature", {@link Subjects#granter()} for an
 * ability an Aura grants that removes counters from the Aura (Heliod's Punishment; bound at activation). A target
 * subject is {@link RemoveCounterFromTargetPermanentEffect}'s.</p>
 *
 * <p>{@link CounterType#ANY} is "remove a counter from this creature" of any kind: only with the source subject and a
 * fixed amount, of a kind the source has at least that many of; with several such kinds the controller chooses one as
 * the effect resolves.</p>
 *
 * <p>It is a REMOVE_COUNTERS step: "If you do" / "When you do" after it is a {@code ConditionalEffect} of
 * {@code HappenedThisWay.removedCounters()} (or {@code atLeast(REMOVE_COUNTERS, n)} for "remove N").</p>
 */
public record RemoveCounterFromSourceEffect(CounterType counterType, int amount, DynamicAmount dynamicAmount,
                                            boolean targetsPlayer, PermanentSubject subject) implements CardEffect {

    public RemoveCounterFromSourceEffect {
        if (subject == null) {
            subject = Subjects.source();
        }
        if (subject.declaresTarget()) {
            throw new IllegalArgumentException(
                    "A target subject is RemoveCounterFromTargetPermanentEffect's: " + subject);
        }
        if (targetsPlayer && !(subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Only the source form targets a player for its amount: " + subject);
        }
        if (counterType == CounterType.ANY
                && (dynamicAmount != null || targetsPlayer || !(subject instanceof PermanentSubject.Source))) {
            throw new IllegalArgumentException("A counter of any kind is removed only from the source, a fixed number");
        }
    }

    public RemoveCounterFromSourceEffect(CounterType counterType, int amount, DynamicAmount dynamicAmount,
                                         boolean targetsPlayer) {
        this(counterType, amount, dynamicAmount, targetsPlayer, Subjects.source());
    }

    public RemoveCounterFromSourceEffect(CounterType counterType, int amount) {
        this(counterType, amount, null, false);
    }

    public RemoveCounterFromSourceEffect(CounterType counterType, DynamicAmount dynamicAmount) {
        this(counterType, 0, dynamicAmount, false);
    }

    public RemoveCounterFromSourceEffect(CounterType counterType, DynamicAmount dynamicAmount,
                                         boolean targetsPlayer) {
        this(counterType, 0, dynamicAmount, targetsPlayer);
    }

    /** "Remove {amount} {counterType} counters from {subject}", which it doesn't target. */
    public RemoveCounterFromSourceEffect(PermanentSubject subject, CounterType counterType, int amount) {
        this(counterType, amount, null, false, subject);
    }

    /** "Remove a {counterType} counter from {subject}", which it doesn't target. */
    public RemoveCounterFromSourceEffect(PermanentSubject subject, CounterType counterType) {
        this(subject, counterType, 1);
    }

    /** This effect on {@code newSubject} instead. */
    public RemoveCounterFromSourceEffect withSubject(PermanentSubject newSubject) {
        return new RemoveCounterFromSourceEffect(counterType, amount, dynamicAmount, targetsPlayer, newSubject);
    }

    @Override
    public TargetSpec targetSpec() {
        if (targetsPlayer) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return subject instanceof PermanentSubject.Source
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.NONE;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
