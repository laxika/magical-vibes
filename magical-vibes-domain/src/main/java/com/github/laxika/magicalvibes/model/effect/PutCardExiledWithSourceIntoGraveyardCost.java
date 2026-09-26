package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Activation cost that puts one card exiled with the source permanent into its owner's graveyard.
 * The selected exile card is removed before the activated ability is put onto the stack.
 */
public record PutCardExiledWithSourceIntoGraveyardCost(CardPredicate filter) implements CostEffect {

    public PutCardExiledWithSourceIntoGraveyardCost() {
        this(null);
    }
}
