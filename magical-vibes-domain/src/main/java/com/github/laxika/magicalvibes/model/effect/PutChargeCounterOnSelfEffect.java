package com.github.laxika.magicalvibes.model.effect;

public record PutChargeCounterOnSelfEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
