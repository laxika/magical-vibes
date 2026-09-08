package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CounterType;

/** The number of counters of {@code counterType} on the source card's stack object. */
public record CountersOnStackEntryCard(CounterType counterType) implements DynamicAmount {
}
