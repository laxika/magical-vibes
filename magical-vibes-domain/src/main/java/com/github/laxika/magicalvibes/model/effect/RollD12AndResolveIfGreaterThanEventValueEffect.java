package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Rolls one d12 and resolves {@code wrapped} if the result is greater than the stack entry's
 * event value or is 12. The event value is supplied by the trigger collector, such as combat
 * damage dealt by the triggering event.
 */
public record RollD12AndResolveIfGreaterThanEventValueEffect(CardEffect wrapped) implements CardEffect {

    public RollD12AndResolveIfGreaterThanEventValueEffect {
        Objects.requireNonNull(wrapped, "wrapped effect");
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
