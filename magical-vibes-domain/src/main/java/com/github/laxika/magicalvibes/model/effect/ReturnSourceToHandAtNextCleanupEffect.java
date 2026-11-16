package com.github.laxika.magicalvibes.model.effect;

/**
 * Delayed rider: "Return this permanent to its owner's hand at the beginning of the next cleanup
 * step" (Thawing Glaciers).
 *
 * <p>Resolving this effect only makes a delayed triggered ability (CR 603.7a) that triggers as the next cleanup step
 * begins (CR 514.3a) and returns the source to its owner's hand when it resolves. If the permanent has left the
 * battlefield by then nothing happens.
 */
public record ReturnSourceToHandAtNextCleanupEffect() implements CardEffect {
}
