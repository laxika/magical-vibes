package com.github.laxika.magicalvibes.model.amount;

/** The number of opponents with at least the specified number of poison counters. */
public record OpponentsWithAtLeastPoisonCounters(int minimum) implements DynamicAmount {
}
