package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player shuffles all cards from their hand and all permanents they own into their library,
 * then draws that many cards." (The Great Aurora)
 *
 * <p>The draw count is read per player from what that player actually shuffled in — hand cards plus
 * owned permanents. Tokens are shuffled in and then cease to exist (CR 111.7), but they still count
 * toward the number of cards drawn.
 *
 * <p>Distinct from {@link EachPlayerShufflesZonesIntoLibraryEffect} (Timetwister): this
 * one takes the battlefield rather than the graveyard, and the draw count is dynamic.
 */
public record EachPlayerShufflesHandAndPermanentsIntoLibraryAndDrawsEffect() implements CardEffect {
}
