package com.github.laxika.magicalvibes.model.effect;

public record MayPayLandDropEffect(CardEffect wrapped, String prompt, CardEffect elseEffect)
        implements CardEffect {

    public MayPayLandDropEffect(CardEffect wrapped, String prompt) {
        this(wrapped, prompt, null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (wrapped != null && wrapped.targetSpec() != TargetSpec.NONE) {
            return wrapped.targetSpec();
        }
        return elseEffect == null ? TargetSpec.NONE : elseEffect.targetSpec();
    }
}
