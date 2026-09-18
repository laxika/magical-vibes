package com.github.laxika.magicalvibes.model.effect;

/**
 * Death-trigger-only condition for wording that requires the dying permanent to have been a
 * nonland creature. The death collector evaluates both characteristics from the permanent's
 * snapshotted status as it died.
 */
public record DyingPermanentWasNonlandCreatureConditionalEffect(CardEffect wrapped)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
