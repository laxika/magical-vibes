package com.github.laxika.magicalvibes.model.effect;

public record SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower, boolean trackSacrificedToughness, boolean excludeSelf) implements CostEffect {
    public SacrificeCreatureCost() {
        this(false, false, false, false);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue) {
        this(trackSacrificedManaValue, false, false, false);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower) {
        this(trackSacrificedManaValue, trackSacrificedPower, false, false);
    }

    public SacrificeCreatureCost(boolean trackSacrificedManaValue, boolean trackSacrificedPower, boolean trackSacrificedToughness) {
        this(trackSacrificedManaValue, trackSacrificedPower, trackSacrificedToughness, false);
    }

}
