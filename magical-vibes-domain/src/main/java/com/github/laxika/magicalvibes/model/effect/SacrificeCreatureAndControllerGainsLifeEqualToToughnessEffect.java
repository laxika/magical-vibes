package com.github.laxika.magicalvibes.model.effect;

public record SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
