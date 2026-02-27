package com.github.laxika.magicalvibes.model.effect;

public record UntapSelfEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
