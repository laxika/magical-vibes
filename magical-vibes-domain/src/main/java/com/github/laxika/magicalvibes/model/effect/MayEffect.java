package com.github.laxika.magicalvibes.model.effect;

public record MayEffect(CardEffect wrapped, String prompt) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
