package com.github.laxika.magicalvibes.model.effect;

/** A forced cost paid by flipping a fixed number of coins. */
public record FlipCoinsCost(int count) implements CostEffect {

    public FlipCoinsCost {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }
}
