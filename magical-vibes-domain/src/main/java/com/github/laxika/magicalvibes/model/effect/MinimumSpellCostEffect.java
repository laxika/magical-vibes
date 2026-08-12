package com.github.laxika.magicalvibes.model.effect;

/**
 * Static battlefield effect that makes a spell's mana component cost at least a fixed amount.
 * The effect is applied after ordinary spell-cost increases and reductions.
 */
public record MinimumSpellCostEffect(int minimumMana) implements CardEffect {

    public MinimumSpellCostEffect {
        if (minimumMana < 0) {
            throw new IllegalArgumentException("Minimum spell cost cannot be negative");
        }
    }
}
