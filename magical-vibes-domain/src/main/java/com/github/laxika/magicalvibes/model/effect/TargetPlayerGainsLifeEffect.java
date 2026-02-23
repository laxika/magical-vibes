package com.github.laxika.magicalvibes.model.effect;

public record TargetPlayerGainsLifeEffect(int amount) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
