package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Puts counters on a target permanent and, when at least one counter was placed, queues a
 * reflexive triggered ability. The optional dynamic amount is evaluated after the counters are
 * placed and carried to the reflexive ability as its X value.
 */
public record PutCountersOnTargetPermanentThenReflexiveEffect(
        CounterType counterType,
        int count,
        DynamicAmount reflexiveXValue,
        CardEffect reflexiveEffect,
        boolean reflexiveOptionalTarget
) implements CardEffect {

    public PutCountersOnTargetPermanentThenReflexiveEffect(CounterType counterType, int count,
                                                           CardEffect reflexiveEffect) {
        this(counterType, count, null, reflexiveEffect, false);
    }

    public PutCountersOnTargetPermanentThenReflexiveEffect(CounterType counterType, int count,
                                                           DynamicAmount reflexiveXValue,
                                                           CardEffect reflexiveEffect) {
        this(counterType, count, reflexiveXValue, reflexiveEffect, false);
    }

    public PutCountersOnTargetPermanentThenReflexiveEffect {
        Objects.requireNonNull(counterType, "counterType");
        Objects.requireNonNull(reflexiveEffect, "reflexiveEffect");
    }

    /** The initial target is the creature receiving the counters; the reflexive effect targets later. */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
