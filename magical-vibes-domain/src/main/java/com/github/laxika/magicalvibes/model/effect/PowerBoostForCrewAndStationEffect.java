package com.github.laxika.magicalvibes.model.effect;

/** Static power bonus used while a creature crews a Vehicle or stations a permanent. */
public record PowerBoostForCrewAndStationEffect(int powerBonus)
        implements CrewAndSaddlePowerModifierEffect, StationPowerModifierEffect {

    @Override
    public boolean usesToughnessInsteadOfPower() {
        return false;
    }
}
