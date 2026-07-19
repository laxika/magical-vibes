package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for one white card, one blue card, one black card, one red card,
 * and one green card (five different cards, one per colour, chosen in that order). Each found card is
 * revealed and put into the controller's hand; the library is shuffled once after every colour has
 * been searched. A colour with no matching card in the library is skipped, and the controller may
 * always fail to find. Used by Conflux.
 */
public record SearchLibraryForOneCardOfEachColorToHandEffect() implements CardEffect {
}
