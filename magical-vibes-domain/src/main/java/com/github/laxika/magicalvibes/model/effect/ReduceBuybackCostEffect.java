package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the generic mana component of buyback costs paid by spells while this effect's source
 * is on the battlefield.
 */
public record ReduceBuybackCostEffect(int amount) implements CardEffect {

    public ReduceBuybackCostEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
    }
}
