package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player may pay any amount of mana; then every player
 * mills cards equal to the total amount of mana paid this way.
 *
 * <p>The payment is a single pass in turn order, and the per-player choices are resolved before
 * the shared mill count is applied. Used by Shared Trauma.
 */
public record EachPlayerPaysAnyManaThenMillsEffect() implements CardEffect {
}
