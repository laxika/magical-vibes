package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Pays any amount of energy and puts that many counters on this source permanent. */
public record PayAnyAmountOfEnergyToPutCountersOnSelfEffect(CounterType counterType)
        implements CardEffect {
}
