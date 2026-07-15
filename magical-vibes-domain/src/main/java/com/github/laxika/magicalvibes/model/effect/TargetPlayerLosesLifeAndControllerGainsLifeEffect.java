package com.github.laxika.magicalvibes.model.effect;

public record TargetPlayerLosesLifeAndControllerGainsLifeEffect(int lifeLoss, int lifeGain) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
