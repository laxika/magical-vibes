package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for Static Orb: "As long as this artifact is untapped, players can't untap more
 * than two permanents during their untap steps."
 *
 * <p>While any permanent carrying this effect is untapped, the untap step pauses to let the active
 * player choose up to two of the permanents that would otherwise untap; only those untap that step.
 */
public record StaticOrbEffect() implements CardEffect {
}
