package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns every card exiled with the source permanent to its owner's hand.
 *
 * @param onlyControllerOwned when true, return only cards owned by the ability controller
 */
public record PutAllCardsExiledWithSourceIntoOwnersHandsEffect(boolean onlyControllerOwned) implements CardEffect {

    public PutAllCardsExiledWithSourceIntoOwnersHandsEffect() {
        this(false);
    }
}
