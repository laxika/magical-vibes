package com.github.laxika.magicalvibes.model.effect;

public record PsychicTransferEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
