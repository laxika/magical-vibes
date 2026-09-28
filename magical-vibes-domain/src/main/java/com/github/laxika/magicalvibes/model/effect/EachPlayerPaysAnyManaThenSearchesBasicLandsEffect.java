package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player, starting with the resolving spell's controller, may pay any amount of mana; then
 * each player searches their library for up to the total amount paid in basic land cards and puts
 * them onto the battlefield tapped.
 */
public record EachPlayerPaysAnyManaThenSearchesBasicLandsEffect() implements CardEffect {
}
