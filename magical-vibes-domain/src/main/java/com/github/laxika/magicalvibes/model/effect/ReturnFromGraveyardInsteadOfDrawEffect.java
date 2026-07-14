package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if the controller would draw a card, they return a card from their
 * graveyard to their hand instead; if they can't (empty graveyard), they lose the game.
 * Used by Forbidden Crypt. Detected in {@code DrawService.resolveDrawCard} for the drawing player.
 */
public record ReturnFromGraveyardInsteadOfDrawEffect() implements CardEffect {
}
