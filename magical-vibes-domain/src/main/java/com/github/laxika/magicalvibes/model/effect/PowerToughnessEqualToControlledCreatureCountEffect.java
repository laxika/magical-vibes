package com.github.laxika.magicalvibes.model.effect;

public record PowerToughnessEqualToControlledCreatureCountEffect() implements CardEffect {
    @Override
    public boolean isPowerToughnessDefining() { return true; }
}
