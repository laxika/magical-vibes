package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Rolls a d4 and resolves the wrapped effect only when the result is 1. */
public record RollD4Effect(CardEffect onOne) implements CardEffect {

    public RollD4Effect {
        Objects.requireNonNull(onOne, "onOne");
    }

    @Override
    public TargetSpec targetSpec() {
        return onOne.targetSpec();
    }
}
