package com.github.laxika.magicalvibes.model.effect;

public record MillHalfLibraryEffect(boolean roundUp) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
