package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Puts dynamically computed counters on every permanent created earlier in this resolution. */
public record PutCountersOnCreatedPermanentsEffect(CounterType counterType, DynamicAmount amount)
        implements CardEffect {
}
