package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers a mandatory resolution-time choice among creature cards put into the controller's
 * graveyard by the preceding effect on the same spell, then returns the chosen card to the
 * battlefield under the controller's control.
 */
public record ReturnOneCreatureCardPutIntoGraveyardThisWayEffect() implements CardEffect {
}
