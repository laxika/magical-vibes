package com.github.laxika.magicalvibes.model.effect;

/** Wraps a continuous effect that functions while its source card is in a graveyard. */
public record WrappedGraveyardStaticEffect(CardEffect wrapped) implements GraveyardStaticEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
