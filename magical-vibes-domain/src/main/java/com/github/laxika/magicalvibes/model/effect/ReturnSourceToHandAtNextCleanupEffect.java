package com.github.laxika.magicalvibes.model.effect;

/**
 * Delayed rider: "Return this permanent to its owner's hand at the beginning of the next cleanup
 * step" (Thawing Glaciers).
 *
 * <p>Resolving this effect only schedules a delayed cleanup trigger ({@code DelayedCleanupTrigger}) that
 * returns the source to its owner's hand when it resolves. If the permanent has left the battlefield
 * by then nothing happens.
 */
public record ReturnSourceToHandAtNextCleanupEffect() implements CardEffect {
}
