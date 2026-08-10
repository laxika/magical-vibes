package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the source permanent to be sacrificed at the beginning of the next cleanup step.
 * Operates on the source, so it carries no target.
 */
public record SacrificeSelfAtNextCleanupEffect() implements CardEffect {
}
