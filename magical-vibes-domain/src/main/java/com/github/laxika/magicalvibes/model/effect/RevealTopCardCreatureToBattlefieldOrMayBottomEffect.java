package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. If it's a creature card, put it onto the
 * battlefield. Otherwise, the controller may put that card on the bottom of their library.
 *
 * <p>Used by Lurking Predators.
 */
public record RevealTopCardCreatureToBattlefieldOrMayBottomEffect() implements CardEffect {
}
