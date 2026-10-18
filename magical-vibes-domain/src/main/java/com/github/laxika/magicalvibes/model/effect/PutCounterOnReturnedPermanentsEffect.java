package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts counters on each permanent returned to the battlefield by an earlier effect of the same
 * resolution, after they have entered ("Put a +1/+1 counter on each of those creatures").
 * Unlike the counters of a return effect that make a permanent enter with them, these arrive
 * after the permanent has entered, so enter triggers see the permanent without them.
 */
public record PutCounterOnReturnedPermanentsEffect(CounterType counterType, int count) implements CardEffect {
}
