package com.github.laxika.magicalvibes.model.effect;

public record LookAtHandEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.PLAYER); }
}
