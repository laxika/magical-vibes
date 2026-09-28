package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/** Matches cards whose mana value is at most the number of counters on the source permanent. */
public record CardManaValueAtMostSourceCountersPredicate(CounterType counterType)
        implements CardPredicate {
}
