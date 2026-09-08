package com.github.laxika.magicalvibes.model.effect;

/**
 * Wraps a creature-death effect whose trigger condition is "one or more" matching creatures dying
 * simultaneously.
 */
public record OneOrMoreCreatureDeathTriggerEffect(CardEffect wrapped)
        implements BatchedCreatureDeathTriggerEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
