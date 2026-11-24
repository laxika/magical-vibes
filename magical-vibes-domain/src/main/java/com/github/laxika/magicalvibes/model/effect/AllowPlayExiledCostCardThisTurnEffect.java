package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may play it" of the card exiled as this ability's cost, with no duration: the play is offered as the ability
 * resolves (CR 608.2g). "You may play that card this turn" is {@link AllowPlayCardsFromExileEffect#thisTurn} over
 * {@link Subjects#exiledAsCost()}.
 */
public record AllowPlayExiledCostCardThisTurnEffect() implements CardEffect {
}
