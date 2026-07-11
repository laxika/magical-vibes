package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * The number of counters of the given type on the source permanent. Reads the stack
 * entry's source permanent (or its last-known snapshot when the source left the
 * battlefield as part of a cost, e.g. "Sacrifice ~: ... for each charge counter on it").
 */
public record CountersOnSource(CounterType counterType) implements DynamicAmount {
}
