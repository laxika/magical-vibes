package com.github.laxika.magicalvibes.model.effect;

public record ShuffleGraveyardIntoLibraryEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
