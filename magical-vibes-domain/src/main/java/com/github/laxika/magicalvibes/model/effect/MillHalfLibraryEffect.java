package com.github.laxika.magicalvibes.model.effect;

public record MillHalfLibraryEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
