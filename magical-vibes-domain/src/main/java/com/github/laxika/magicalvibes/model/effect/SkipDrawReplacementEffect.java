package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability marker for a static replacement effect that replaces every card draw with no draw.
 *
 * <p>The draw service checks this global replacement for each player before performing a draw.
 */
public interface SkipDrawReplacementEffect extends CardEffect {
}
