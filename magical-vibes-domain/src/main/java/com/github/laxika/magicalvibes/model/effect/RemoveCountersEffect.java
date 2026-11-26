package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Remove up to {@code amount} counters of the given type from each permanent {@code subject} names, clamped at zero
 * (no-op on a permanent that has none).
 *
 * <p>{@code subject} is {@link Subjects#source()} by default ("remove a counter from this creature":
 * {@link #fromSelf(CounterType, int)} and its overloads). {@link Subjects#target()} is "remove a counter from target
 * permanent" ({@link #fromTarget()} and its overloads), whose {@code targetPredicate} narrows the legal targets. A
 * subject can also name a permanent without targeting it: {@link Subjects#attachedAtTrigger()} for Venarian Gold's
 * "remove a sleep counter from that creature", {@link Subjects#attached()} for "enchanted creature",
 * {@link Subjects#granter()} for an ability an Aura grants that removes counters from the Aura (Heliod's Punishment;
 * bound at activation), {@code thisWay(CHOOSE)} for "from each of any number of", or a set such as
 * {@code controlledBy(you(), creature)} for "from each creature you control".</p>
 *
 * <p>A dynamic amount is evaluated on resolution: for a target against the ability's source, otherwise against the
 * permanent the counters come off. The player-targeting form ({@link #fromSelfCountingTargetPlayer}) uses the stack
 * entry's target player for that amount while still removing counters from the source permanent.</p>
 *
 * <p>{@link CounterType#ANY} is "remove a counter" of any kind: only from the source or a target, a fixed number, of a
 * kind the permanent has at least that many of; with several such kinds the controller chooses one as the effect
 * resolves.</p>
 *
 * <p>{@code all} is "remove all [kind] counters" ({@link #allFromSelf}, {@link #allFrom}, {@link #allFromTarget}): every
 * counter of the kind each permanent has as it resolves, {@code amount} unused. It can always be done, also from a
 * permanent with none ("if no counters were removed this way", Bounty of the Luxa), so a "may" or a mode is never
 * refused for want of counters. "That many" after it is {@code AmountThisWay(REMOVE_COUNTERS)}.</p>
 *
 * <p>Each removal is made by the effect's controller ("when you remove the last ... counter"); the last time counter
 * triggers vanishing and a Siege's last defense counter its defeat. It is a REMOVE_COUNTERS step: "If you do" /
 * "When you do" after it is a {@code ConditionalEffect} of {@code HappenedThisWay.removedCounters()} (or
 * {@code atLeast(REMOVE_COUNTERS, n)} for "remove N").</p>
 */
public record RemoveCountersEffect(CounterType counterType, int amount, DynamicAmount dynamicAmount,
                                   boolean targetsPlayer, PermanentSubject subject,
                                   PermanentPredicate targetPredicate, boolean all) implements CardEffect {

    public RemoveCountersEffect {
        if (subject == null) {
            subject = Subjects.source();
        }
        if (targetsPlayer && !(subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Only the source form targets a player for its amount: " + subject);
        }
        if (targetPredicate != null && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("A target restriction needs the target subject: " + subject);
        }
        if (subject.declaresTarget() && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("Only target() declares a target here: " + subject);
        }
        if (all && (counterType == CounterType.ANY || dynamicAmount != null || targetsPlayer || amount != 0)) {
            throw new IllegalArgumentException("\"Remove all\" names one kind and no amount: " + counterType);
        }
        if (counterType == CounterType.ANY
                && (dynamicAmount != null || targetsPlayer
                || !(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Target))) {
            throw new IllegalArgumentException(
                    "A counter of any kind is removed only from the source or a target, a fixed number");
        }
    }

    public RemoveCountersEffect(CounterType counterType, int amount, DynamicAmount dynamicAmount,
                                boolean targetsPlayer, PermanentSubject subject, PermanentPredicate targetPredicate) {
        this(counterType, amount, dynamicAmount, targetsPlayer, subject, targetPredicate, false);
    }

    public RemoveCountersEffect(CounterType counterType, int amount, DynamicAmount dynamicAmount,
                                boolean targetsPlayer, PermanentSubject subject) {
        this(counterType, amount, dynamicAmount, targetsPlayer, subject, null);
    }

    /** "Remove {amount} {counterType} counters from {subject}", which it doesn't target. */
    public RemoveCountersEffect(PermanentSubject subject, CounterType counterType, int amount) {
        this(counterType, amount, null, false, subject);
    }

    /** "Remove a {counterType} counter from {subject}", which it doesn't target. */
    public RemoveCountersEffect(PermanentSubject subject, CounterType counterType) {
        this(subject, counterType, 1);
    }

    /** "Remove N [kind] counters from this [permanent]". */
    public static RemoveCountersEffect fromSelf(CounterType counterType, int amount) {
        return new RemoveCountersEffect(counterType, amount, null, false, Subjects.source());
    }

    /** "Remove X [kind] counters from this [permanent]", {@code amount} evaluated against the source as it resolves. */
    public static RemoveCountersEffect fromSelf(CounterType counterType, DynamicAmount amount) {
        return new RemoveCountersEffect(counterType, 0, amount, false, Subjects.source());
    }

    /**
     * {@link #fromSelf(CounterType, DynamicAmount)} in an ability that targets a player the amount counts for
     * ("remove a +1/+1 counter from this for each card in target opponent's hand").
     */
    public static RemoveCountersEffect fromSelfCountingTargetPlayer(CounterType counterType, DynamicAmount amount) {
        return new RemoveCountersEffect(counterType, 0, amount, true, Subjects.source());
    }

    /** "Remove a counter from target permanent": one counter of any kind, chosen as it resolves among several. */
    public static RemoveCountersEffect fromTarget() {
        return fromTarget(CounterType.ANY, 1);
    }

    /** "Remove N [kind] counters from target permanent". */
    public static RemoveCountersEffect fromTarget(CounterType counterType, int amount) {
        return fromTarget(counterType, amount, null);
    }

    /** "Remove a [kind] counter from target [permanent matching {@code targetPredicate}]". */
    public static RemoveCountersEffect fromTarget(CounterType counterType, PermanentPredicate targetPredicate) {
        return fromTarget(counterType, 1, targetPredicate);
    }

    /** "Remove N [kind] counters from target [permanent matching {@code targetPredicate}]". */
    public static RemoveCountersEffect fromTarget(CounterType counterType, int amount,
                                                  PermanentPredicate targetPredicate) {
        return new RemoveCountersEffect(counterType, amount, null, false, Subjects.target(), targetPredicate);
    }

    /**
     * "Remove X [kind] counters from target [permanent matching {@code targetPredicate}]", {@code amount} evaluated
     * against the ability's source as it resolves ("remove twice X loyalty counters").
     */
    public static RemoveCountersEffect fromTarget(CounterType counterType, DynamicAmount amount,
                                                  PermanentPredicate targetPredicate) {
        return new RemoveCountersEffect(counterType, 0, amount, false, Subjects.target(), targetPredicate);
    }

    /** "Remove all [kind] counters from this [permanent]". */
    public static RemoveCountersEffect allFromSelf(CounterType counterType) {
        return allFrom(Subjects.source(), counterType);
    }

    /**
     * "Remove all [kind] counters from {@code subject}", which it doesn't target: "from it" ({@code triggering()}),
     * "from all creatures" ({@code controlledBy(eachPlayer(), creature)}).
     */
    public static RemoveCountersEffect allFrom(PermanentSubject subject, CounterType counterType) {
        return new RemoveCountersEffect(counterType, 0, null, false, subject, null, true);
    }

    /** "Remove all [kind] counters from target [permanent matching {@code targetPredicate}]". */
    public static RemoveCountersEffect allFromTarget(CounterType counterType, PermanentPredicate targetPredicate) {
        return new RemoveCountersEffect(counterType, 0, null, false, Subjects.target(), targetPredicate, true);
    }

    /** This effect on {@code newSubject} instead. */
    public RemoveCountersEffect withSubject(PermanentSubject newSubject) {
        return new RemoveCountersEffect(counterType, amount, dynamicAmount, targetsPlayer, newSubject,
                newSubject instanceof PermanentSubject.Target ? targetPredicate : null, all);
    }

    /** This effect removing counters of {@code kind} (the kind chosen for {@link CounterType#ANY}). */
    public RemoveCountersEffect withKind(CounterType kind) {
        return new RemoveCountersEffect(kind, amount, null, false, subject, targetPredicate, all);
    }

    /**
     * How many counters this removes from {@code permanent} ({@code fixedOrEvaluated} being the fixed or evaluated
     * amount): all of the kind it has for {@link #all}.
     */
    public int amountFor(Permanent permanent, int fixedOrEvaluated) {
        return all ? permanent.getCounterCount(counterType) : fixedOrEvaluated;
    }

    @Override
    public TargetSpec targetSpec() {
        if (targetsPlayer) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (subject instanceof PermanentSubject.Target) {
            return TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
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
