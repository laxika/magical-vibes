package com.github.laxika.magicalvibes.model.effect;

/**
 * "If a player would planeswalk as a result of rolling the planar die, chaos ensues instead": the replacement floated by
 * {@link GrantStaticEffectToPlayerEffect#untilYourNextTurn} (Fixed Point in Time), read for whoever's roll it is. It has
 * no handler of its own.
 */
public record PlanarDiePlaneswalkToChaosEffect() implements PlanarDiePlaneswalkToChaosReplacementEffect {
}
