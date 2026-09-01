package com.github.laxika.magicalvibes.model.effect;

/** Wraps an effect that belongs only to a source's "when you sacrifice this" trigger. */
public record SacrificeOnlyEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return true;
    }
}
