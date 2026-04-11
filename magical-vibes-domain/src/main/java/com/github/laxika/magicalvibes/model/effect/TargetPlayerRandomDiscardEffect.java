package com.github.laxika.magicalvibes.model.effect;

public record TargetPlayerRandomDiscardEffect(int amount, boolean causedByOpponent) implements CardEffect {

    public TargetPlayerRandomDiscardEffect() {
        this(1, true);
    }

    public TargetPlayerRandomDiscardEffect(int amount) {
        this(amount, false);
    }

    @Override
    public boolean canTargetPlayer() {
        return causedByOpponent;
    }
}
