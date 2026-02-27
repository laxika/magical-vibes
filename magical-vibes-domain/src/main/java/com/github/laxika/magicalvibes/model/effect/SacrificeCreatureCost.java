package com.github.laxika.magicalvibes.model.effect;

public record SacrificeCreatureCost() implements CostEffect {
    @Override public boolean canTargetPermanent() { return true; }
}
