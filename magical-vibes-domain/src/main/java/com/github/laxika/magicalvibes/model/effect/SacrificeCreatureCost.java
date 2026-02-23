package com.github.laxika.magicalvibes.model.effect;

public record SacrificeCreatureCost() implements CardEffect {
    @Override public boolean canTargetPermanent() { return true; }
}
