package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that adds a draw before a creature connives.
 *
 * <p>The connive event itself is still resolved normally after the replacement draw.
 */
public record DrawBeforeConniveReplacementEffect() implements CardEffect {
}
