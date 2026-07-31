package com.github.laxika.magicalvibes.model.amount;

/**
 * The total life gained this turn, summed over the players in scope. Reads
 * {@code GameData.lifeGainedThisTurn}, which accumulates per-player life gain and is cleared at the
 * start of each turn. Used by Voracious Wurm ("enters with X +1/+1 counters, where X is the amount
 * of life you've gained this turn" — {@link CountScope#CONTROLLER}).
 */
public record LifeGainedThisTurn(CountScope scope) implements DynamicAmount {
}
