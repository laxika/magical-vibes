package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Offers to put one matching face-up card owned by the spell's target from exile into its graveyard. */
public record PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect(CardPredicate filter) implements CardEffect {

    /** Offers any face-up card owned by the spell's target. */
    public PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect() {
        this(null);
    }
}
