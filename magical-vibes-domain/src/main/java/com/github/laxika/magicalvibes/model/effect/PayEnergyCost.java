package com.github.laxika.magicalvibes.model.effect;

/** Pays energy counters as an activated-ability, forced, or non-mana alternative spell cost. */
public record PayEnergyCost(int amount) implements AlternativeSpellCost {

    public PayEnergyCost {
        if (amount <= 0) {
            throw new IllegalArgumentException("Energy cost must be positive");
        }
    }

    @Override
    public Kind kind() {
        return Kind.PAY_ENERGY;
    }
}
