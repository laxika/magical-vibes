package com.github.laxika.magicalvibes.model.effect;

/** Uses a qualifying creature's effective toughness instead of its power while stationing. */
public record UseToughnessForStationEffect() implements StationPowerModifierEffect {

    @Override
    public boolean usesToughnessInsteadOfPower() {
        return true;
    }
}
