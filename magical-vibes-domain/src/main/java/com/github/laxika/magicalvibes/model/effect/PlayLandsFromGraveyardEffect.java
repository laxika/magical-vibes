package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static permission to play matching land cards from the controller's graveyard.
 * When {@code oncePerOwnTurn} is set, the permission can be used for only one land during each of its
 * controller's turns (Muldrotha, the Gravetide).
 */
public record PlayLandsFromGraveyardEffect(CardPredicate filter, boolean oncePerOwnTurn)
        implements PlayLandsFromGraveyardPermission {

    public PlayLandsFromGraveyardEffect() {
        this(null, false);
    }

    public PlayLandsFromGraveyardEffect(CardPredicate filter) {
        this(filter, false);
    }

    @Override
    public CardPredicate landFilter() {
        return filter;
    }

    @Override
    public boolean oncePerControllerTurnForLand() {
        return oncePerOwnTurn;
    }

    @Override
    public boolean onlyDuringControllerTurn() {
        return oncePerOwnTurn;
    }
}
