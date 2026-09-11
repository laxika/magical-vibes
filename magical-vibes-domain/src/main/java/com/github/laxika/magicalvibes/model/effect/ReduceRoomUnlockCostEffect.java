package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the generic mana component of Room-door unlock costs paid by the source permanent's
 * controller.
 */
public record ReduceRoomUnlockCostEffect(int amount) implements CardEffect {

    public ReduceRoomUnlockCostEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
    }
}
