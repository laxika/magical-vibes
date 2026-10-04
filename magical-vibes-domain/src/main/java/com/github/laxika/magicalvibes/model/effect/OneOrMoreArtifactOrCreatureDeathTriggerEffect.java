package com.github.laxika.magicalvibes.model.effect;

/** Wraps an effect whose trigger condition is one or more controlled artifacts and/or creatures dying simultaneously. */
public record OneOrMoreArtifactOrCreatureDeathTriggerEffect(CardEffect wrapped)
        implements BatchedArtifactOrCreatureDeathTriggerEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
