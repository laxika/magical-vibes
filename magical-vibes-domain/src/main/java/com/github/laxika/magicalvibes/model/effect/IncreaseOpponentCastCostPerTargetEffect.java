package com.github.laxika.magicalvibes.model.effect;

/**
 * Increases opponents' spells' generic casting costs by {@code amount} for every chosen target.
 */
public record IncreaseOpponentCastCostPerTargetEffect(int amount)
        implements PerTargetCastCostIncreaseEffect {
}
