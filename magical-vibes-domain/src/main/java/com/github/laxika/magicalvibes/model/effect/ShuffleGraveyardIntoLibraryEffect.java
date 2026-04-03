package com.github.laxika.magicalvibes.model.effect;

public record ShuffleGraveyardIntoLibraryEffect(boolean targetPlayer) implements CardEffect {
    @Override public boolean canTargetPlayer() { return targetPlayer; }
}
