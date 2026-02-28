package com.github.laxika.magicalvibes.model.effect;

public record SwitchPowerToughnessEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
