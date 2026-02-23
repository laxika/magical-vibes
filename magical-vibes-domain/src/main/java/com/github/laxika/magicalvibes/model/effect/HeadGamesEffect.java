package com.github.laxika.magicalvibes.model.effect;

public record HeadGamesEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
