package com.github.laxika.magicalvibes.model.effect;

/**
 * Static self-effect: the source creature gains each of a fixed set of "watched" keywords for as
 * long as a creature card with that keyword is in any graveyard. Models Cairn Wanderer's ability
 * (flying, fear, first strike, double strike, deathtouch, haste, landwalk, lifelink, reach,
 * trample, shroud, and vigilance). Protection is not granted because inherent protection is not
 * modelled as a card characteristic in this engine.
 */
public record GainKeywordsOfCreatureCardsInAllGraveyardsEffect() implements CardEffect {
}
