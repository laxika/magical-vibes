package com.github.laxika.magicalvibes.model.effect;

public record RevealTopCardOfLibraryEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
