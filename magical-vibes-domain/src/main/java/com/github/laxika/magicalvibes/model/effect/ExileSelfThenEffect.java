package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile this permanent. If you do, {@code thenEffect}." Exiles the stack entry's source
 * permanent and resolves {@code thenEffect} only when the exile actually happened.
 *
 * @param thenEffect the payload resolved after a successful exile
 */
public record ExileSelfThenEffect(CardEffect thenEffect) implements CardEffect {

    public ExileSelfThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("ExileSelfThenEffect requires a payload; use ExileSelfEffect for a bare exile");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec payload = thenEffect.targetSpec();
        return new TargetSpec(payload.declaredTarget(), payload.harmful(), payload.predicate(), true,
                payload.playerTargetCount());
    }
}
