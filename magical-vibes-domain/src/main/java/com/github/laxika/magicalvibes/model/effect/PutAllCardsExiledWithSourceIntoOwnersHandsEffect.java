package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns every card exiled with the source permanent to its owner's hand.
 *
 * @param onlyControllerOwned when true, return only cards owned by the ability controller
 * @param onlyIntelCounters   when true, return only cards marked with intel counters
 */
public record PutAllCardsExiledWithSourceIntoOwnersHandsEffect(boolean onlyControllerOwned,
                                                                boolean onlyIntelCounters) implements CardEffect {

    public PutAllCardsExiledWithSourceIntoOwnersHandsEffect() {
        this(false, false);
    }

    public PutAllCardsExiledWithSourceIntoOwnersHandsEffect(boolean onlyControllerOwned) {
        this(onlyControllerOwned, false);
    }
}
