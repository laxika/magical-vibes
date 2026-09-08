package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the creature type stored on the source permanent.
 *
 * <p>The source may already have left the battlefield, so resolution reads the type from the
 * stack entry's source snapshot.
 */
public record RevealSourceChosenSubtypeEffect() implements CardEffect {
}
