package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a creature type, then reveals the top card of the controller's library. A matching
 * creature card goes to hand; any other card goes to the controller's graveyard.
 *
 * <p>Changeling cards match every chosen creature type.</p>
 */
public record RevealTopCardOfChosenCreatureTypeToHandElseGraveyardEffect() implements CardEffect {
}
