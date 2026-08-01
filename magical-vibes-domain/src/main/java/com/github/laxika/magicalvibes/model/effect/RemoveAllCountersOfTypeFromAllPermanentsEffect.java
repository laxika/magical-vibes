package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Removes all counters of {@code counterType} from every permanent on every battlefield.
 * Corrosion's leaves-the-battlefield cleanup ("remove all rust counters from all permanents").
 */
public record RemoveAllCountersOfTypeFromAllPermanentsEffect(CounterType counterType)
        implements CardEffect {
}
