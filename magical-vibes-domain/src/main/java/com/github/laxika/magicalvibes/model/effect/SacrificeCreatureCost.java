package com.github.laxika.magicalvibes.model.effect;

public record SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower) implements CostEffect {
    public SacrificeCreatureCost() {
        this(false, false);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue) {
        this(trackSacrificedManaValue, false);
    }

    @Override public boolean canTargetPermanent() { return true; }
}
