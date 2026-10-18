package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * The number of counters of the given type on the stack entry's target permanent; a {@code null}
 * counter type counts counters of every kind ("the number of counters on it").
 * Evaluates to 0 when there is no target or the permanent is no longer on the battlefield.
 */
public record CountersOnTargetPermanent(CounterType counterType) implements DynamicAmount {
}
