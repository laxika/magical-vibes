package com.github.laxika.magicalvibes.model.effect;

public record DealDamageToAnyTargetEffect(int damage, boolean cantRegenerate) implements CardEffect {

    public DealDamageToAnyTargetEffect(int damage) {
        this(damage, false);
    }

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
