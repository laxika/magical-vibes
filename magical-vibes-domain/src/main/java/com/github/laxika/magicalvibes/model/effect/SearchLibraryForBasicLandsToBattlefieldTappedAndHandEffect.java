package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for up to two basic land cards, reveals them,
 * puts one onto the battlefield tapped and the other into the controller's hand,
 * then shuffles. Used by Cultivate, Kodama's Reach, etc.
 */
public record SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect() implements CardEffect {
}
