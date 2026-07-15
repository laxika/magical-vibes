package com.github.laxika.magicalvibes.model.effect;

public record MillHalfLibraryEffect(boolean roundUp) implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.PLAYER); }
}
