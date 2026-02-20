package com.github.laxika.magicalvibes.model.effect;

public record ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect(int minimumCreatureDifference, int amount)
        implements CardEffect {
}
