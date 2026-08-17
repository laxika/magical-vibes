package com.github.laxika.magicalvibes.model.effect;

/** Uses the source creature's effective toughness instead of its power for crew and saddle costs. */
public record UseToughnessForCrewAndSaddleEffect() implements CrewAndSaddlePowerModifierEffect {

    @Override
    public int powerBonus() {
        return 0;
    }

    @Override
    public boolean usesToughnessInsteadOfPower() {
        return true;
    }
}
