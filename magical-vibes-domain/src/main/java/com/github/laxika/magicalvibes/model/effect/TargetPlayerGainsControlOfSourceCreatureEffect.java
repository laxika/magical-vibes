package com.github.laxika.magicalvibes.model.effect;

public record TargetPlayerGainsControlOfSourceCreatureEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
