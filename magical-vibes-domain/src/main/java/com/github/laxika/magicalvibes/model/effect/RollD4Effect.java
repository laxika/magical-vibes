package com.github.laxika.magicalvibes.model.effect;

/** Rolls a d4 and resolves the configured result-dependent follow-up effects. */
public record RollD4Effect(CardEffect onOne, CardEffect onResult) implements CardEffect {

    public RollD4Effect(CardEffect onOne) {
        this(onOne, null);
    }

    public RollD4Effect {
        if (onOne == null && onResult == null) {
            throw new IllegalArgumentException("RollD4Effect requires a follow-up effect");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        if (onResult != null && onResult.targetSpec().declaredTarget() != null) {
            return onResult.targetSpec();
        }
        if (onOne != null) {
            return onOne.targetSpec();
        }
        return onResult.targetSpec();
    }
}
