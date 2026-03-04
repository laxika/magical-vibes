package com.github.laxika.magicalvibes.model.effect;

public record PutChargeCounterOnTargetPermanentEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() { return true; }
}
