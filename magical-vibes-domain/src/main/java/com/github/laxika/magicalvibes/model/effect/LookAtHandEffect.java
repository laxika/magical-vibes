package com.github.laxika.magicalvibes.model.effect;

public record LookAtHandEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
