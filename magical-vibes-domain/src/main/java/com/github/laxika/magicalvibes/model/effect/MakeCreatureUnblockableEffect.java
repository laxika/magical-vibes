package com.github.laxika.magicalvibes.model.effect;

public record MakeCreatureUnblockableEffect(boolean selfTargeting) implements CardEffect {

    public MakeCreatureUnblockableEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return selfTargeting
                ? new TargetSpec(TargetCategory.NONE, false, null, true, 1)
                : TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
