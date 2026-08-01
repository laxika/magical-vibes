package com.github.laxika.magicalvibes.model.effect;

/**
 * "Shuffle all cards from your graveyard into your library. Target player mills that many cards."
 *
 * <p>The mill count is the number of cards that actually moved from the controller's graveyard into
 * their library, so it must be captured between the two halves — the graveyard is empty afterwards
 * and the milled cards land in the target's graveyard after the shuffle. Used by Psychic Spiral.</p>
 */
public record ShuffleControllerGraveyardIntoLibraryThenMillTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
