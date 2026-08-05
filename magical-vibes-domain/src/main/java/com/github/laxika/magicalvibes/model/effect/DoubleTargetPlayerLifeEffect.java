package com.github.laxika.magicalvibes.model.effect;

public record DoubleTargetPlayerLifeEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.player()); }
}
