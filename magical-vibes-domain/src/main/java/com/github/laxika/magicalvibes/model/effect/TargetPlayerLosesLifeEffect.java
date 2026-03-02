package com.github.laxika.magicalvibes.model.effect;

public record TargetPlayerLosesLifeEffect(int amount) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
