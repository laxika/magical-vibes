package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes each opponent with at least {@code minimumPoisonCounters} poison counters lose life.
 */
public record EachOpponentWithPoisonCountersLosesLifeEffect(int amount, int minimumPoisonCounters)
        implements CardEffect {
}
