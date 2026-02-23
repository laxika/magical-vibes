package com.github.laxika.magicalvibes.model.effect;

public record DealXDamageToAnyTargetAndGainXLifeEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
