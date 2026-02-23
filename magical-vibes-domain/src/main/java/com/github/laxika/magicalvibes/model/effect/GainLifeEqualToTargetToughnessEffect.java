package com.github.laxika.magicalvibes.model.effect;

public record GainLifeEqualToTargetToughnessEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
