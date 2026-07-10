package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for Storage Matrix: "As long as this artifact is untapped, each player chooses
 * artifact, creature, or land during their untap step. That player can untap only permanents of
 * the chosen type this step."
 *
 * <p>While any permanent carrying this effect is untapped, the untap step pauses to let the active
 * player choose one of artifact/creature/land; only permanents of the chosen type untap that step.
 */
public record StorageMatrixEffect() implements CardEffect {
}
