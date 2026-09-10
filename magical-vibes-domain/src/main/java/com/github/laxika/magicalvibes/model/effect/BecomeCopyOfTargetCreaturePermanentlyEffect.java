package com.github.laxika.magicalvibes.model.effect;

/** Makes the source permanent a permanent copy of a target creature. */
public record BecomeCopyOfTargetCreaturePermanentlyEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
