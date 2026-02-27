package com.github.laxika.magicalvibes.model.effect;

public record PowerToughnessEqualToControlledLandCountEffect() implements CardEffect {
    @Override
    public boolean isPowerToughnessDefining() { return true; }
}
