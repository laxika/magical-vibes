package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. Return all creature cards of the chosen type from your graveyard to the
 * battlefield." (Bloodline Bidding).
 *
 * <p>On resolution the controller chooses a creature type, then the effect returns up to
 * {@code maxCount} matching creature cards from their graveyard. Changeling cards match every
 * creature type. {@link Integer#MAX_VALUE} means all matching cards.</p>
 */
public record ReturnCreaturesOfChosenTypeFromGraveyardEffect(int maxCount) implements CardEffect {

    public ReturnCreaturesOfChosenTypeFromGraveyardEffect() {
        this(Integer.MAX_VALUE);
    }
}
