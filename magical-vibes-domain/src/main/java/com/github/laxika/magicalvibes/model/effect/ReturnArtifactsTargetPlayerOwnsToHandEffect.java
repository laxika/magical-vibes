package com.github.laxika.magicalvibes.model.effect;

public record ReturnArtifactsTargetPlayerOwnsToHandEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
