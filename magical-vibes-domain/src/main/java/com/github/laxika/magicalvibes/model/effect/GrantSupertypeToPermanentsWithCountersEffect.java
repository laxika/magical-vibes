package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;

/** Static effect that grants a supertype to every permanent with one or more specified counters. */
public record GrantSupertypeToPermanentsWithCountersEffect(CounterType counterType,
                                                           CardSupertype supertype)
        implements CardEffect {
}
