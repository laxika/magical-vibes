package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that requires putting a fixed number of cards from the controller's graveyard on
 * the bottom of that player's library.
 *
 * <p>No selection is optional: the cost is unpayable when the controller's graveyard contains
 * fewer than the required number of cards.
 */
public record PutCardsFromGraveyardOnBottomOfLibraryCost(int count) implements CostEffect {

    public PutCardsFromGraveyardOnBottomOfLibraryCost {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }
}
