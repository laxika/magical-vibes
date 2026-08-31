package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Taps the source permanent and queues {@code thenEffect} only if the source was newly tapped. */
public record TapSourceThenEffect(CardEffect thenEffect) implements CardEffect {

    public TapSourceThenEffect {
        Objects.requireNonNull(thenEffect, "thenEffect");
    }

    /**
     * The payload is selected by the reflexive ability after the source is tapped, so its target
     * must not be exposed as a target of the enclosing effect.
     */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
