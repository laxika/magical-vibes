package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule every permanent created earlier in this same resolution to be exiled at the beginning
 * of the next cleanup step.
 *
 * <p>Reads {@code StackEntry.createdPermanentIds}, so it only ever acts on permanents created by
 * this resolution. Pair it after a token-creating effect for effects such as Waylay.
 */
public record ExileCreatedPermanentsAtNextCleanupEffect() implements CardEffect {
}
