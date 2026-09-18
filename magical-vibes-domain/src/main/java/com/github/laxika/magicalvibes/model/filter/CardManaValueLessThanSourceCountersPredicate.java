package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/** Matches cards whose mana value is less than the number of counters on the source permanent. */
public record CardManaValueLessThanSourceCountersPredicate(CounterType counterType)
        implements CardPredicate {
}
