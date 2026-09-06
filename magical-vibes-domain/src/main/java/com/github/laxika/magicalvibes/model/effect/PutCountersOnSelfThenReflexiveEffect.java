package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.Objects;

/**
 * Puts counters on the source permanent and, when at least one counter was placed, queues a
 * reflexive triggered ability. An optional condition is checked when the counter is placed and
 * again when the reflexive ability resolves.
 */
public record PutCountersOnSelfThenReflexiveEffect(
        CounterType counterType,
        int count,
        Condition condition,
        CardEffect reflexiveEffect
) implements CardEffect {

    public PutCountersOnSelfThenReflexiveEffect(CounterType counterType, CardEffect reflexiveEffect) {
        this(counterType, 1, null, reflexiveEffect);
    }

    public PutCountersOnSelfThenReflexiveEffect(CounterType counterType, int count,
                                                CardEffect reflexiveEffect) {
        this(counterType, count, null, reflexiveEffect);
    }

    public PutCountersOnSelfThenReflexiveEffect {
        Objects.requireNonNull(counterType, "counterType");
        Objects.requireNonNull(reflexiveEffect, "reflexiveEffect");
    }

    /** The reflexive payload chooses its own targets after the counter placement succeeds. */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
