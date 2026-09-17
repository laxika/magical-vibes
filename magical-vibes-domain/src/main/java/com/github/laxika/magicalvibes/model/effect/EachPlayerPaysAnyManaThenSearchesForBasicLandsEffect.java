package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player may pay any amount of mana; then every player
 * searches for up to that shared total in basic land cards and puts them onto the battlefield
 * tapped.
 */
public record EachPlayerPaysAnyManaThenSearchesForBasicLandsEffect() implements CardEffect {
}
