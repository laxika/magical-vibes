package com.github.laxika.magicalvibes.model.effect;

/**
 * Cyclone's upkeep ability: put a wind counter on the source, then pay {G} for each wind counter
 * or sacrifice it. If the payment succeeds, the source deals damage equal to its wind counters to
 * each creature and each player.
 */
public record CycloneUpkeepEffect() implements CardEffect {
}
