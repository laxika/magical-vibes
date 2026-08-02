package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Cost effect for "Remove one or more counters from among creatures you control". The number of
 * counters is the activation-time {@code xValue} (the ability must be marked
 * {@link com.github.laxika.magicalvibes.model.ActivatedAbility#withXValue()}), and the counters may
 * be split freely across the controller's creatures.
 *
 * <p>The variable-count, board-wide sibling of {@link RemoveOneOrMoreCountersFromSourceCost} and the
 * variable-count sibling of {@link RemoveCounterFromControlledCreatureCost}. Effects on the same
 * ability read the number removed with {@link com.github.laxika.magicalvibes.model.amount.XValue}
 * (Ooze Flux).
 *
 * @param counterType type of counter to remove
 */
public record RemoveOneOrMoreCountersFromControlledCreaturesCost(CounterType counterType) implements CostEffect {
}
