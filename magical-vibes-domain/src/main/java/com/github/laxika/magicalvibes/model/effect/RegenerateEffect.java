package com.github.laxika.magicalvibes.model.effect;

public record RegenerateEffect(boolean targetsPermanent) implements RegenerationEffect {

    public RegenerateEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPermanent
                ? TargetSpec.benign(TargetCategory.PERMANENT)
                : new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
