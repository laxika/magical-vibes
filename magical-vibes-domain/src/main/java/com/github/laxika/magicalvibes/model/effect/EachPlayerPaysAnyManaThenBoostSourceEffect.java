package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player may pay any amount of mana; then the source
 * gets +X/+0 until end of turn, where X is the total amount of mana paid this way.
 *
 * <p>The payment is a single pass in turn order, and each player's choice is made before the
 * shared boost is applied. Used by Mana-Charged Dragon.
 */
public record EachPlayerPaysAnyManaThenBoostSourceEffect() implements CardEffect {
}
