package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each creature you control is the chosen type in addition to its other types.
 * Reads the chosen subtype from the source permanent's {@code chosenSubtype} field.
 * Used by Xenograft, Arcane Adaptation, and similar effects.
 */
public record GrantChosenSubtypeToOwnCreaturesEffect() implements CardEffect {
}
