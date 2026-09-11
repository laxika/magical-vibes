package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost that puts one card an opponent owns from exile into that player's graveyard.
 */
public record PutOpponentOwnedExiledCardIntoGraveyardCost() implements CostEffect {

    @Override
    public boolean putsOpponentOwnedExiledCardIntoGraveyard() {
        return true;
    }
}
