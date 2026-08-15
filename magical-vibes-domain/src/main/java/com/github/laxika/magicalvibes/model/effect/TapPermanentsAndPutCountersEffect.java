package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses any number of their untapped permanents matching {@code filter}, taps
 * those permanents, and puts one counter of {@code counterType} on each permanent tapped this way.
 */
public record TapPermanentsAndPutCountersEffect(PermanentPredicate filter, CounterType counterType,
                                                String prompt) implements CardEffect {
}
