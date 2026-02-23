package com.github.laxika.magicalvibes.model.effect;

public record GainControlOfTargetAuraEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
