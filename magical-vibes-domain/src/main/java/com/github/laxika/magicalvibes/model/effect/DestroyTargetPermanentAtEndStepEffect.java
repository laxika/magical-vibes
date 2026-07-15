package com.github.laxika.magicalvibes.model.effect;

public record DestroyTargetPermanentAtEndStepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
