package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces this spell's generic casting cost by {@code amount} for every chosen target.
 */
public record ReduceOwnCastCostForEachTargetEffect(int amount)
        implements TargetCountCastCostReductionEffect {
}
