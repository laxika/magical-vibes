package com.github.laxika.magicalvibes.model.effect;

/**
 * Delayed rider: "Return this permanent to its owner's hand at the beginning of the next cleanup
 * step" (Thawing Glaciers).
 *
 * <p>Resolving this effect only schedules the bounce — it flags the source permanent
 * {@code returnToHandAtNextCleanup}, and {@code TurnCleanupService} performs the actual return
 * during the cleanup step. If the permanent has left the battlefield by then nothing happens.
 */
public record ReturnSourceToHandAtNextCleanupEffect() implements CardEffect {
}
