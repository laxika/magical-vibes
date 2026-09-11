package com.github.laxika.magicalvibes.model.effect;

/** Reduces the generic mana portion of ninjutsu abilities by a fixed amount. */
public record ReduceNinjutsuCostEffect(int amount) implements NinjutsuCostReducingEffect {

    public ReduceNinjutsuCostEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }
}
