package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Cumulative-upkeep cost that puts one counter on a creature an opponent controls for each of
 * {@code count} age-counter payments. The payer chooses the opponent's creature separately for
 * each payment.
 */
public record PutCounterOnOpponentCreatureCost(CounterType counterType, int count) implements CostEffect {
}
