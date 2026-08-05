package com.github.laxika.magicalvibes.model.effect;

public record PsychicTransferEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.player()); }
}
