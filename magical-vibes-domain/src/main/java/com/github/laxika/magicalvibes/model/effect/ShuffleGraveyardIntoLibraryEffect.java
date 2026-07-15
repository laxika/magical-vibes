package com.github.laxika.magicalvibes.model.effect;

public record ShuffleGraveyardIntoLibraryEffect(boolean targetPlayer) implements CardEffect {
    @Override public TargetSpec targetSpec() {
        return targetPlayer ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }
}
