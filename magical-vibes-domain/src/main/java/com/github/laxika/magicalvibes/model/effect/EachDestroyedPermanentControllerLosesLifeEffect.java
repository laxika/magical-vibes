package com.github.laxika.magicalvibes.model.effect;

/**
 * Rider for {@link DestroyAllPermanentsEffect}: each player loses life equal to the number of
 * permanents they controlled that were actually destroyed by the preceding wipe.
 *
 * <p>The per-player tally is read from {@code StackEntry.eventPlayerIds}, where the destroy-all
 * handler records one controller id for every permanent that reached a graveyard. This is life
 * loss, not damage, and indestructible or regenerated permanents do not contribute.
 */
public record EachDestroyedPermanentControllerLosesLifeEffect() implements CardEffect {
}
