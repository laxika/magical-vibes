package com.github.laxika.magicalvibes.model.effect;

public record DoubleTargetPlayerLifeEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
