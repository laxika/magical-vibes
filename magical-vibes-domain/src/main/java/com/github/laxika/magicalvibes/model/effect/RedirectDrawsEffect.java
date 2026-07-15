package com.github.laxika.magicalvibes.model.effect;

public record RedirectDrawsEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.PLAYER); }
}
