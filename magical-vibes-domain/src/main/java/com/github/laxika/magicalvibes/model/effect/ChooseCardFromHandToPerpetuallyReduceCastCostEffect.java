package com.github.laxika.magicalvibes.model.effect;

/** Chooses a nonland card from hand and perpetually reduces its generic spell cost. */
public record ChooseCardFromHandToPerpetuallyReduceCastCostEffect(int amount) implements CardEffect {

    public ChooseCardFromHandToPerpetuallyReduceCastCostEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("Perpetual cast-cost reduction must be positive");
        }
    }
}
