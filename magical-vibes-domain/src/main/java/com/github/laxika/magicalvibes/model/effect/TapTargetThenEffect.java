package com.github.laxika.magicalvibes.model.effect;

/**
 * Taps the first target and resolves the payload only when that target was newly tapped.
 * This models a mandatory "tap target ... If you do, ..." contingency; a plain sequence would
 * incorrectly resolve the payload when the target became tapped before resolution.
 *
 * <p>The effect must be bound to a single target group. The payload resolves against the same
 * stack entry, so payloads that read additional target groups can still use them.</p>
 *
 * @param thenEffect the payload resolved after a successful tap
 */
public record TapTargetThenEffect(CardEffect thenEffect) implements CardEffect {

    public TapTargetThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("TapTargetThenEffect requires a payload");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
