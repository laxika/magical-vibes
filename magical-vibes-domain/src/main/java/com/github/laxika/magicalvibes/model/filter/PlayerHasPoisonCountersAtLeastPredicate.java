package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches players with at least {@code minimum} poison counters ("each opponent who has three or more poison
 * counters", Feed the Infection, Phyrexian Atlas). Evaluated against {@code GameData.playerPoisonCounters}.
 */
public record PlayerHasPoisonCountersAtLeastPredicate(int minimum) implements PlayerPredicate {
}
