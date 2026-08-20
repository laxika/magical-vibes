package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Distributes a dynamic number of counters among creatures controlled by the effect's controller.
 * The distribution is chosen during resolution and does not use targets.
 */
public record DistributeCountersAmongControlledCreaturesEffect(
        CounterType counterType, DynamicAmount total) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
