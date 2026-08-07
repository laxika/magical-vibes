package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect paid by drawing {@code count} cards (Psychic Vortex's cumulative upkeep — "Draw a
 * card"). Always payable: drawing from an empty library is a legal payment, the payer simply loses
 * the game the next time state-based actions are checked.
 *
 * @param count how many cards the payer draws (one per age counter for cumulative upkeep)
 */
public record DrawCardsCost(int count) implements CostEffect {

    public DrawCardsCost {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
    }
}
