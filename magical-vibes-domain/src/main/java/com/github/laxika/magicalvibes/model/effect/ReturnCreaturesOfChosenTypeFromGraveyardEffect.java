package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. Return all creature cards of the chosen type from your graveyard to the
 * battlefield." (Bloodline Bidding).
 *
 * <p>On resolution the controller chooses a creature type, then the effect returns every matching
 * creature card from their graveyard. Changeling cards match every creature type.</p>
 */
public record ReturnCreaturesOfChosenTypeFromGraveyardEffect() implements CardEffect {
}
