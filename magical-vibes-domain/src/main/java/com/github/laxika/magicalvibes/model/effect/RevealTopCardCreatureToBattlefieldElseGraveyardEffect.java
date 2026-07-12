package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. If it's a creature card, put it onto the
 * battlefield. Otherwise, put it into the controller's graveyard.
 *
 * <p>Used by Call of the Wild.
 */
public record RevealTopCardCreatureToBattlefieldElseGraveyardEffect() implements CardEffect {
}
