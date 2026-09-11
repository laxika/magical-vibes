package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Distributes a dynamic number of counters among creatures controlled by the effect's controller.
 * The distribution is chosen during resolution and does not target the recipients.
 * The target-player variant uses its permanent predicate to admit recipients such as Vehicles.
 */
public record DistributeCountersAmongControlledCreaturesEffect(
        CounterType counterType, DynamicAmount total, PermanentPredicate permanentFilter,
        boolean targetsPlayer) implements CardEffect {

    public DistributeCountersAmongControlledCreaturesEffect(
            CounterType counterType, DynamicAmount total, PermanentPredicate permanentFilter) {
        this(counterType, total, permanentFilter, false);
    }

    /** Distributes among matching permanents controlled by the targeted player. */
    public DistributeCountersAmongControlledCreaturesEffect forTargetPlayer() {
        return new DistributeCountersAmongControlledCreaturesEffect(counterType, total, permanentFilter, true);
    }

    public DistributeCountersAmongControlledCreaturesEffect(CounterType counterType, DynamicAmount total) {
        this(counterType, total, null);
    }

    public DistributeCountersAmongControlledCreaturesEffect(CounterType counterType, int total) {
        this(counterType, new Fixed(total), null);
    }

    public DistributeCountersAmongControlledCreaturesEffect(CounterType counterType, int total,
                                                             PermanentPredicate permanentFilter) {
        this(counterType, new Fixed(total), permanentFilter);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
