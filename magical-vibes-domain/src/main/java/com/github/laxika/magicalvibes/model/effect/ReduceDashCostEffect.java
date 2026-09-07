package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the generic mana component of dash costs paid by the source permanent's controller.
 */
public record ReduceDashCostEffect(int amount) implements CardEffect {

    public ReduceDashCostEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
    }
}
