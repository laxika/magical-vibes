package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution-time optional life payment that resolves {@code wrapped} when accepted and payable.
 */
public record MayPayLifeEffect(int lifeCost, CardEffect wrapped, String prompt) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped == null ? TargetSpec.NONE : wrapped.targetSpec();
    }
}
