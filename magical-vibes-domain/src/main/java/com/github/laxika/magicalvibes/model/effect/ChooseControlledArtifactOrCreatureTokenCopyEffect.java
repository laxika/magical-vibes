package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses an artifact or creature they control during resolution, then creates a
 * token that's a copy of it. The choice is not a spell target.
 */
public record ChooseControlledArtifactOrCreatureTokenCopyEffect() implements CardEffect {
}
