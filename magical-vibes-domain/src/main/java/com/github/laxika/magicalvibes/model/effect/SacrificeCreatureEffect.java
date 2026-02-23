package com.github.laxika.magicalvibes.model.effect;

public record SacrificeCreatureEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
