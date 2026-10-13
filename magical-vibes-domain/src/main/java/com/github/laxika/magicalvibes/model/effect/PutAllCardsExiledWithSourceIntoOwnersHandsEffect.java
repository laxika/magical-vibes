package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns cards exiled with the source permanent to their owners' hands.
 *
 * @param onlyControllerOwned when true, return only cards owned by the ability controller
 * @param onlyIntelCounters   when true, return only cards marked with intel counters
 * @param anySourcePermanent  when true, the source permanent is not required: matching cards exiled with any
 *                            permanent are returned, including ones whose exiling permanent has left
 */
public record PutAllCardsExiledWithSourceIntoOwnersHandsEffect(boolean onlyControllerOwned,
                                                                boolean onlyIntelCounters,
                                                                boolean anySourcePermanent) implements CardEffect {

    public PutAllCardsExiledWithSourceIntoOwnersHandsEffect() {
        this(false, false, false);
    }

    public PutAllCardsExiledWithSourceIntoOwnersHandsEffect(boolean onlyControllerOwned) {
        this(onlyControllerOwned, false, false);
    }

    public PutAllCardsExiledWithSourceIntoOwnersHandsEffect(boolean onlyControllerOwned, boolean onlyIntelCounters) {
        this(onlyControllerOwned, onlyIntelCounters, false);
    }
}
