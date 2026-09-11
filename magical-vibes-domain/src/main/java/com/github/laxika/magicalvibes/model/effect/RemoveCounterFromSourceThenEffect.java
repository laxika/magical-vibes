package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Removes counters from the source permanent and, if they were removed, puts a reflexive
 * triggered ability with {@code thenEffect} on the stack.
 *
 * <p>This is useful for "you may remove a counter from this. When you do, ..." abilities. The
 * follow-up is deliberately not exposed as the outer effect's target: a reflexive trigger chooses
 * its target only after the counter has actually been removed.
 *
 * @param counterType the counter kind to remove; {@link CounterType#ANY} chooses a present kind
 * @param count the number of counters to remove
 * @param thenEffect the effect of the reflexive triggered ability
 * @param onlyIfLastCounterRemoved whether the reflexive ability is created only when the removed
 *                                 counter was the last counter of the requested type
 */
public record RemoveCounterFromSourceThenEffect(CounterType counterType, int count, CardEffect thenEffect,
                                                boolean onlyIfLastCounterRemoved)
        implements CardEffect {

    public RemoveCounterFromSourceThenEffect(CounterType counterType, CardEffect thenEffect) {
        this(counterType, 1, thenEffect, false);
    }

    public RemoveCounterFromSourceThenEffect(CounterType counterType, CardEffect thenEffect,
                                             boolean onlyIfLastCounterRemoved) {
        this(counterType, 1, thenEffect, onlyIfLastCounterRemoved);
    }

    public RemoveCounterFromSourceThenEffect(CounterType counterType, int count, CardEffect thenEffect) {
        this(counterType, count, thenEffect, false);
    }

    public RemoveCounterFromSourceThenEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
