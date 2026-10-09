package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Rolls a d8 and resolves the configured result-dependent follow-up effect. */
public record RollD8Effect(CardEffect onResult, boolean activationCost) implements CardEffect {

    public RollD8Effect(CardEffect onResult) {
        this(onResult, false);
    }

    public RollD8Effect {
        Objects.requireNonNull(onResult, "Follow-up effect cannot be null");
    }

    @Override
    public TargetSpec targetSpec() {
        return onResult.targetSpec();
    }
}
