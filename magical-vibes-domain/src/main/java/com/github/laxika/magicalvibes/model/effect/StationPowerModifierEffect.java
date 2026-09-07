package com.github.laxika.magicalvibes.model.effect;

/** Capability for a static ability that changes the power used by a station action. */
public interface StationPowerModifierEffect extends CardEffect {

    /** Whether effective toughness replaces effective power for stationing. */
    default boolean usesToughnessInsteadOfPower() {
        return false;
    }
}
