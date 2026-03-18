package com.github.laxika.magicalvibes.model.effect;

public record DealDamageEqualToSourcePowerToAnyTargetEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
