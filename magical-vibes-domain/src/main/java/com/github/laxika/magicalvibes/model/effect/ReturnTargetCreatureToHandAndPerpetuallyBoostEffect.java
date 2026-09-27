package com.github.laxika.magicalvibes.model.effect;

/** Returns the target creature and records a power/toughness boost for that card. */
public record ReturnTargetCreatureToHandAndPerpetuallyBoostEffect(int powerBoost, int toughnessBoost)
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
