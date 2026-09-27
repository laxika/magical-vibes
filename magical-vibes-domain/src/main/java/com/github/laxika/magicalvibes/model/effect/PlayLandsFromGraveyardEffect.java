package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static permission to play matching land cards from the controller's graveyard. */
public record PlayLandsFromGraveyardEffect(CardPredicate filter) implements PlayLandsFromGraveyardPermission {

    public PlayLandsFromGraveyardEffect() {
        this(null);
    }

    @Override
    public CardPredicate landFilter() {
        return filter;
    }
}
