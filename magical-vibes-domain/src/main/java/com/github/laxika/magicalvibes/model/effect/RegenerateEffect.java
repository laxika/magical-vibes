package com.github.laxika.magicalvibes.model.effect;

public record RegenerateEffect(boolean targetsPermanent) implements CardEffect {

    public RegenerateEffect() {
        this(false);
    }

    @Override
    public boolean canTargetPermanent() {
        return targetsPermanent;
    }

    @Override
    public boolean isSelfTargeting() { return !targetsPermanent; }
}
