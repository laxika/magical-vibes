package com.github.laxika.magicalvibes.model.effect;

/** Pays energy counters as an activated-ability or forced pay-or-fallback cost. */
public record PayEnergyCost(int amount) implements CostEffect {

    public PayEnergyCost {
        if (amount <= 0) {
            throw new IllegalArgumentException("Energy cost must be positive");
        }
    }
}
