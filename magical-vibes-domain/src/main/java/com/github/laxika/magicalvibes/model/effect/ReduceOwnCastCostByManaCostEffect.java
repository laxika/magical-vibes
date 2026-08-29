package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaCost;

/**
 * Reduces this spell's own casting cost by the given mana cost, including colored mana symbols.
 */
public record ReduceOwnCastCostByManaCostEffect(ManaCost reduction) implements CardEffect {

    public ReduceOwnCastCostByManaCostEffect(String reduction) {
        this(new ManaCost(reduction));
    }
}
