package com.github.laxika.magicalvibes.model.effect;

public record SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower, boolean trackSacrificedToughness) implements CostEffect {
    public SacrificeCreatureCost() {
        this(false, false, false);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue) {
        this(trackSacrificedManaValue, false, false);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower) {
        this(trackSacrificedManaValue, trackSacrificedPower, false);
    }

    @Override public boolean canTargetPermanent() { return true; }
}
