package com.github.laxika.magicalvibes.model.effect;

public record SacrificeCreatureCost(boolean trackSacrificedManaValue) implements CostEffect {
    public SacrificeCreatureCost() {
        this(false);
    }

    @Override public boolean canTargetPermanent() { return true; }
}
