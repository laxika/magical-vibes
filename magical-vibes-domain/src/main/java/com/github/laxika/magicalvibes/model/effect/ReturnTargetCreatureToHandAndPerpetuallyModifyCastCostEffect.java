package com.github.laxika.magicalvibes.model.effect;

/** Returns the target creature and changes that card's generic cast cost perpetually. */
public record ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect(int amount, boolean increase)
        implements RemovalEffect {

    public ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("Perpetual cast-cost change must be positive");
        }
    }

    public static ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect reduceCost(int amount) {
        return new ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect(amount, false);
    }

    public static ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect increaseCost(int amount) {
        return new ReturnTargetCreatureToHandAndPerpetuallyModifyCastCostEffect(amount, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
