package com.github.laxika.magicalvibes.model.effect;

/** Capability for a static ability that changes the power used by a station action. */
public interface StationPowerModifierEffect extends CardEffect {

    /** The power added while paying a station cost. */
    default int powerBonus() {
        return 0;
    }

    /** Whether effective toughness replaces effective power for stationing. */
    default boolean usesToughnessInsteadOfPower() {
        return false;
    }
}
