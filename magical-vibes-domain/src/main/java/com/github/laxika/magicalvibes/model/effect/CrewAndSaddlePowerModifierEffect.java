package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static ability that modifies the source creature's power only while it is
 * being used to crew a Vehicle or saddle a Mount.
 */
public interface CrewAndSaddlePowerModifierEffect extends CardEffect {

    /** The power added while paying a crew or saddle cost. */
    int powerBonus();

    /** Whether the source's effective toughness replaces its effective power for that cost. */
    default boolean usesToughnessInsteadOfPower() {
        return false;
    }
}
