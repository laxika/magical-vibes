package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the source permanent to its back face, or back to its front face if already transformed.
 * Used by double-faced cards with the Transform keyword.
 */
public record TransformSelfEffect() implements CardEffect {
}
