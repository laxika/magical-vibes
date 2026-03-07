package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the controller of the targeted spell lose life.
 * Used as a companion effect alongside counter-spell effects (e.g. Psychic Barrier).
 * Does not independently target — piggybacks on the spell's existing target.
 */
public record TargetSpellControllerLosesLifeEffect(int amount) implements CardEffect {
}
