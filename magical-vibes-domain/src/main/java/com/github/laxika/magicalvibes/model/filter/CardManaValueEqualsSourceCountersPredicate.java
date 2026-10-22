package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/** Matches cards whose mana value equals the number of counters on the source permanent. */
public record CardManaValueEqualsSourceCountersPredicate(CounterType counterType)
        implements CardPredicate {
}
