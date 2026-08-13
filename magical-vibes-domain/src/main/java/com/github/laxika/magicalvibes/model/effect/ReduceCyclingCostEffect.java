package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the generic mana portion of cycling abilities by a fixed amount.
 */
public record ReduceCyclingCostEffect(int amount) implements CyclingCostReducingEffect {

    public ReduceCyclingCostEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }
}
