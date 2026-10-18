package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death trigger for "When this creature dies, you may distribute N {@code counterType} counters
 * among any number of creatures", covering both the dying-source-counters form (Vastwood Hydra —
 * "a number of +1/+1 counters equal to the number of +1/+1 counters on this creature … among any
 * number of creatures you control") and the fixed-total form (Jugan, the Rising Star — "five +1/+1
 * counters among any number of target creatures").
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector resolves {@code count} (for
 * {@code countFromSourceCounters} it snapshots the dying permanent's counters, which are gone by
 * resolution), wraps the baked effect in a {@link MayEffect}, and puts it on the stack. The division
 * is chosen via {@code GameData.pendingETBDamageAssignments}, the same buffer used by Inferno Titan /
 * Gang of Devils divided damage. For targeted and any-creature forms the staged division is announced
 * onto the stack entry when the trigger is put there, and resolution reads that announced division.
 *
 * @param counterType             the counter type placed on the receiving creatures (and, when
 *                                {@code countFromSourceCounters}, counted on the dying creature).
 * @param count                   the total to distribute — the printed total for the fixed form,
 *                                or (for {@code countFromSourceCounters}) 0 on the marker instance
 *                                placed on the card, filled in by the collector.
 * @param countFromSourceCounters when {@code true}, {@code count} is the dying creature's count of
 *                                {@code counterType} rather than a printed total.
 * @param anyCreature             when {@code true}, any creature on the battlefield may receive
 *                                counters (Jugan); when {@code false}, only creatures the trigger's
 *                                controller controls (Vastwood Hydra).
 * @param countFromSourcePower    when {@code true}, the collector snapshots the dying creature's
 *                                effective power rather than its counter count.
 * @param optional                when {@code true}, the collector wraps the distribution in a
 *                                "you may" choice.
 * @param targeted                when {@code true}, the recipients are declared as targets, so
 *                                shroud, hexproof and protection stop them from receiving counters
 *                                (Grand Ossuary).
 */
public record DistributeCountersAmongCreaturesOnDeathEffect(
        CounterType counterType, int count, boolean countFromSourceCounters, boolean anyCreature,
        boolean countFromSourcePower, boolean optional, boolean targeted)
        implements CardEffect {

    public DistributeCountersAmongCreaturesOnDeathEffect(
            CounterType counterType, int count, boolean countFromSourceCounters, boolean anyCreature,
            boolean countFromSourcePower, boolean optional) {
        this(counterType, count, countFromSourceCounters, anyCreature, countFromSourcePower, optional, false);
    }

    public DistributeCountersAmongCreaturesOnDeathEffect(
            CounterType counterType, int count, boolean countFromSourceCounters, boolean anyCreature) {
        this(counterType, count, countFromSourceCounters, anyCreature, false, true, false);
    }

    /**
     * "You may distribute a number of {@code counterType} counters equal to the number of
     * {@code counterType} counters on this creature among any number of creatures you control"
     * (Vastwood Hydra).
     */
    public static DistributeCountersAmongCreaturesOnDeathEffect fromDyingSourceCountersAmongControlledCreatures(
            CounterType counterType) {
        return new DistributeCountersAmongCreaturesOnDeathEffect(counterType, 0, true, false, false, true, false);
    }

    /**
     * "When this creature dies, distribute X {@code counterType} counters among any number of target
     * creatures you control, where X is the number of {@code counterType} counters on this creature"
     * (Quirion Beastcaller). Unlike the Vastwood Hydra form, the division is not optional and the
     * recipients are declared as targets when the trigger goes on the stack.
     */
    public static DistributeCountersAmongCreaturesOnDeathEffect fromDyingSourceCountersAmongTargetControlledCreatures(
            CounterType counterType) {
        return new DistributeCountersAmongCreaturesOnDeathEffect(counterType, 0, true, false, false, false, true);
    }

    /**
     * "Whenever a creature dies, its controller distributes a number of +1/+1 counters equal to its
     * power among any number of target creatures they control" (Grand Ossuary).
     */
    public static DistributeCountersAmongCreaturesOnDeathEffect fromDyingSourcePowerAmongControlledCreatures(
            CounterType counterType) {
        return new DistributeCountersAmongCreaturesOnDeathEffect(
                counterType, 0, false, false, true, false, true);
    }

    /**
     * "You may distribute {@code count} {@code counterType} counters among any number of target
     * creatures" (Jugan, the Rising Star).
     * <p>
     * The printed ability targets, so the receiving creatures and their per-creature amounts are
     * announced as the trigger is put on the stack (CR 601.2d, CR 603.3d). Targeting restrictions such
     * as shroud and protection are checked at that announcement.
     */
    public static DistributeCountersAmongCreaturesOnDeathEffect fixedAmongAnyCreatures(
            CounterType counterType, int count) {
        return new DistributeCountersAmongCreaturesOnDeathEffect(counterType, count, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
