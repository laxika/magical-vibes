package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Remove up to three stun counters from among all permanents", "remove any number of counters from among permanents
 * on the battlefield", "remove any number of +1/+1 counters from among creatures you control": as the effect resolves
 * its controller chooses how many {@code counterType} counters to remove from each permanent {@code permanents}
 * names, at most {@code maxAmount} in total (CR 608.2d). {@link CounterType#ANY} is counters of every kind, each kind
 * on each permanent asked on its own; counters of one kind are interchangeable (CR 122.1), so only the number is
 * asked. None of the permanents is targeted.
 *
 * <p>The questions are one "how many" prompt per permanent and kind ({@code ChoiceContext.MoveCountersAmountChoice}
 * with removal slots); the counters come off together once the last one is answered. A removal is not a move, so
 * nothing about where counters could go limits it. It is a REMOVE_COUNTERS step: it records the total removed, the
 * permanents that lost counters and their controllers, read by {@code AmountThisWay(REMOVE_COUNTERS)} ("equal to the
 * number of counters removed this way") and {@code HappenedThisWay.removedCounters()} ("if you do").</p>
 */
public record RemoveUpToCountersFromAllPermanentsEffect(CounterType counterType, int maxAmount,
                                                        PermanentSubject permanents)
        implements CardEffect {

    public RemoveUpToCountersFromAllPermanentsEffect {
        if (counterType == null) {
            throw new IllegalArgumentException("counterType must not be null");
        }
        if (maxAmount < 0) {
            throw new IllegalArgumentException("maxAmount must not be negative");
        }
        if (permanents == null) {
            permanents = Subjects.allPermanents();
        }
        if (permanents.declaresTarget()) {
            throw new IllegalArgumentException("The permanents counters are removed from are not targets: "
                    + permanents);
        }
    }

    /** "Remove up to {@code maxAmount} {@code counterType} counters from among all permanents." */
    public RemoveUpToCountersFromAllPermanentsEffect(CounterType counterType, int maxAmount) {
        this(counterType, maxAmount, Subjects.allPermanents());
    }

    /** "Remove any number of {@code counterType} counters from among {@code permanents}." */
    public static RemoveUpToCountersFromAllPermanentsEffect anyNumber(CounterType counterType,
                                                                       PermanentSubject permanents) {
        return new RemoveUpToCountersFromAllPermanentsEffect(counterType, Integer.MAX_VALUE, permanents);
    }
}
