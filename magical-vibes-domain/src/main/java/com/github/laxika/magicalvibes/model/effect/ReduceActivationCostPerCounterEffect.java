package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Reduces the generic mana portion of an activated ability's cost by {reductionPerCounter} for each
 * counter of {counterType} on the source permanent (floored at zero generic mana). Carried in the
 * ability's effect list as a {@link CostEffect}, so it is stripped from the resolved effects and only
 * consulted when computing/paying the activation cost. Used by Diary of Dreams ("This ability costs
 * {1} less to activate for each page counter on this artifact").
 */
public record ReduceActivationCostPerCounterEffect(CounterType counterType, int reductionPerCounter)
        implements CostEffect {
}
