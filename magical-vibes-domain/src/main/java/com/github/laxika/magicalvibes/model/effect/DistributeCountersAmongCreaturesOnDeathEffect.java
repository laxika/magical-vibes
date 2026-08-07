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
 * is chosen when the trigger resolves via {@code GameData.pendingETBDamageAssignments}, the same
 * buffer used by Inferno Titan / Gang of Devils divided damage.
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
 */
public record DistributeCountersAmongCreaturesOnDeathEffect(
        CounterType counterType, int count, boolean countFromSourceCounters, boolean anyCreature)
        implements CardEffect {

    /**
     * "You may distribute a number of {@code counterType} counters equal to the number of
     * {@code counterType} counters on this creature among any number of creatures you control"
     * (Vastwood Hydra).
     */
    public static DistributeCountersAmongCreaturesOnDeathEffect fromDyingSourceCountersAmongControlledCreatures(
            CounterType counterType) {
        return new DistributeCountersAmongCreaturesOnDeathEffect(counterType, 0, true, false);
    }

    /**
     * "You may distribute {@code count} {@code counterType} counters among any number of target
     * creatures" (Jugan, the Rising Star).
     * <p>
     * The printed ability targets, but the engine models the distribution the same way it models
     * Vastwood Hydra's: the receiving creatures and their per-creature amounts are chosen as the
     * trigger resolves rather than announced as it is put on the stack (CR 603.3d / CR 601.2d).
     * Practically identical, except that targeting legality (shroud, protection) is not checked.
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
