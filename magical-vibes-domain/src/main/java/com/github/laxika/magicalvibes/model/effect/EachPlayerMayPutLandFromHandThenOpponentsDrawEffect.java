package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player secretly chooses whether to put one land card from their hand onto the battlefield.
 * After all chosen lands enter simultaneously, each opponent of the effect's controller who did
 * not choose a land draws a card.
 */
public record EachPlayerMayPutLandFromHandThenOpponentsDrawEffect() implements CardEffect {
}
