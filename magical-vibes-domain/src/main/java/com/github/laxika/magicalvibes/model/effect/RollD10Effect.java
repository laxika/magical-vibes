package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Rolls a d10 and resolves the configured result-dependent follow-up effect. */
public record RollD10Effect(CardEffect onResult) implements CardEffect {

    public RollD10Effect {
        Objects.requireNonNull(onResult, "onResult");
    }

    @Override
    public TargetSpec targetSpec() {
        return onResult.targetSpec();
    }
}
