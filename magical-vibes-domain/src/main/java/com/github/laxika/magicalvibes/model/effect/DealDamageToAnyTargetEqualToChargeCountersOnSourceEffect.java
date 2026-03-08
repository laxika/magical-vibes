package com.github.laxika.magicalvibes.model.effect;

public record DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
