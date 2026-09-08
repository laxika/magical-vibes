package com.github.laxika.magicalvibes.model;

/**
 * An additional cost that removes the specified counters from among creatures controlled by the
 * player casting the spell. A payment may remove multiple counters from one creature or split them
 * among several creatures.
 */
public record RemoveCountersFromControlledCreaturesCastingCost(int count, CounterType counterType)
        implements CastingCost {
}
