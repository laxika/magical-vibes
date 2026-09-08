package com.github.laxika.magicalvibes.model.effect;

/** Internal may-ability marker for casting one exiled card with a generic cost reduction. */
public record MayCastExiledCardWithCostReductionEffect(int genericCostReduction) implements CardEffect {

    public MayCastExiledCardWithCostReductionEffect {
        if (genericCostReduction < 1) {
            throw new IllegalArgumentException("genericCostReduction must be positive");
        }
    }
}
