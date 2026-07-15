package com.github.laxika.magicalvibes.model.effect;

public record ControlTargetPlayerNextTurnEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.PLAYER); }
}
