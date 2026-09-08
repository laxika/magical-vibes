package com.github.laxika.magicalvibes.model.effect;

/**
 * ON_DEATH value-materialising effect: the controller gains life equal to this permanent's
 * last-known effective power. The power is read when the death trigger is collected and baked into
 * a concrete {@link GainLifeEffect}.
 */
public record GainLifeEqualToDyingSourcePowerEffect() implements CardEffect {
}
