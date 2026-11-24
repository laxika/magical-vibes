package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Remove up to three stun counters from among all permanents", "remove any number of counters from among permanents
 * on the battlefield", "remove up to X counters from target permanent", "remove X counters from target permanent",
 * "remove up to five counters from target artifact, creature, planeswalker, or opponent": as the effect resolves its
 * controller chooses how many {@code counterType} counters to remove from each permanent {@code permanents} names and
 * each player {@code players} names, at most {@code amount} in total (CR 608.2d). {@link CounterType#ANY} is counters
 * of every kind, each kind on each permanent (and each tracked player counter kind on each player) asked on its own;
 * counters of one kind are interchangeable (CR 122.1), so only the number is asked.
 *
 * <p>{@code amount} is evaluated as the effect resolves ({@link com.github.laxika.magicalvibes.model.amount.XValue}
 * for Hex Parasite's X). With {@code exactly} ("remove X counters") the total is {@code amount} or, when fewer are
 * there, every counter: each question's lowest answer is what the later questions can no longer make up, and a
 * question with one possible answer is answered unasked (CR 608.2d).</p>
 *
 * <p>The subjects target only when they are target variants ({@link Subjects#target()}, {@link
 * Subjects#targetPlayer()}); {@code targetFilter} narrows a permanent target ("target creature"). With neither
 * subject given it is {@link Subjects#allPermanents()}.</p>
 *
 * <p>The questions are one "how many" prompt per permanent or player and kind ({@code
 * ChoiceContext.MoveCountersAmountChoice} with removal slots); the counters come off together once the last one is
 * answered. A removal is not a move, so nothing about where counters could go limits it. It is a REMOVE_COUNTERS
 * step: it records the total removed, the permanents that lost counters, their controllers and the players that lost
 * counters, read by {@code AmountThisWay(REMOVE_COUNTERS)} ("for each counter removed this way", "create that many")
 * and {@code HappenedThisWay.removedCounters()} ("if you do").</p>
 */
public record RemoveUpToCountersFromAllPermanentsEffect(CounterType counterType, DynamicAmount amount, boolean exactly,
                                                        PermanentSubject permanents, PermanentPredicate targetFilter,
                                                        PlayerSubject players)
        implements CardEffect {

    public RemoveUpToCountersFromAllPermanentsEffect {
        if (counterType == null) {
            throw new IllegalArgumentException("counterType must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount instanceof Fixed fixed && fixed.value() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        if (permanents == null && players == null) {
            permanents = Subjects.allPermanents();
        }
        if (targetFilter != null && (permanents == null || !permanents.declaresTarget())) {
            throw new IllegalArgumentException("A target filter needs a target subject: " + permanents);
        }
    }

    /** "Remove up to {@code maxAmount} {@code counterType} counters from among {@code permanents}." */
    public RemoveUpToCountersFromAllPermanentsEffect(CounterType counterType, int maxAmount,
                                                     PermanentSubject permanents) {
        this(counterType, new Fixed(maxAmount), false, permanents, null, null);
    }

    /** "Remove up to {@code maxAmount} {@code counterType} counters from among all permanents." */
    public RemoveUpToCountersFromAllPermanentsEffect(CounterType counterType, int maxAmount) {
        this(counterType, maxAmount, Subjects.allPermanents());
    }

    /** "Remove up to {@code amount} {@code counterType} counters from {@code permanents}" with a counted bound. */
    public RemoveUpToCountersFromAllPermanentsEffect(CounterType counterType, DynamicAmount amount,
                                                     PermanentSubject permanents) {
        this(counterType, amount, false, permanents, null, null);
    }

    /** "Remove any number of {@code counterType} counters from among {@code permanents}." */
    public static RemoveUpToCountersFromAllPermanentsEffect anyNumber(CounterType counterType,
                                                                       PermanentSubject permanents) {
        return new RemoveUpToCountersFromAllPermanentsEffect(counterType, Integer.MAX_VALUE, permanents);
    }

    /**
     * "Remove {@code amount} {@code counterType} counters from {@code permanents}" ("remove X counters from target
     * permanent", "remove a counter from target permanent"): that many, or all of them when there are fewer.
     */
    public static RemoveUpToCountersFromAllPermanentsEffect exactly(CounterType counterType, DynamicAmount amount,
                                                                     PermanentSubject permanents) {
        return new RemoveUpToCountersFromAllPermanentsEffect(counterType, amount, true, permanents, null, null);
    }

    /** The same removal from a permanent target narrowed by {@code filter} ("target creature"). */
    public RemoveUpToCountersFromAllPermanentsEffect matchingTarget(PermanentPredicate filter) {
        return new RemoveUpToCountersFromAllPermanentsEffect(counterType, amount, exactly, permanents, filter, players);
    }

    /**
     * The same removal also from {@code players}' counters of every tracked kind ({@code
     * com.github.laxika.magicalvibes.model.PlayerCounterType}): "target artifact, creature, planeswalker, or
     * opponent", one target that may be either, declared by the card's target filter.
     */
    public RemoveUpToCountersFromAllPermanentsEffect orFromPlayers(PlayerSubject players) {
        return new RemoveUpToCountersFromAllPermanentsEffect(counterType, amount, exactly, permanents, targetFilter,
                players);
    }

    @Override
    public TargetSpec targetSpec() {
        boolean permanentTarget = permanents != null && permanents.declaresTarget();
        boolean playerTarget = players != null && players.declaresTarget();
        TargetPredicate permanentPredicate = targetFilter == null
                ? TargetPredicates.permanent() : TargetPredicates.permanents(targetFilter);
        if (permanentTarget && playerTarget) {
            return TargetSpec.benign(targetFilter == null ? TargetPredicates.playerOrPermanent()
                    : TargetPredicates.anyOf(TargetPredicates.player(), permanentPredicate));
        }
        if (playerTarget) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return permanentTarget ? TargetSpec.benign(permanentPredicate) : TargetSpec.NONE;
    }
}
