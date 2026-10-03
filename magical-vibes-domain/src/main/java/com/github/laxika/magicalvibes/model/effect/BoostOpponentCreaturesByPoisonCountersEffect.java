package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives each creature controlled by an opponent -1/-1 for each poison counter that creature's
 * controller has.
 *
 * <p>The amount is evaluated separately for each affected creature because different opponents
 * can have different poison-counter totals.
 */
public record BoostOpponentCreaturesByPoisonCountersEffect() implements CardEffect {
}
