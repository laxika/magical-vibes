package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Static permission to cast one spell milled this turn from the controller's graveyard. */
public record CastMilledSpellFromGraveyardOncePerYourTurnEffect(CardPredicate filter)
        implements CastSpellsFromGraveyardPermission {

    public CastMilledSpellFromGraveyardOncePerYourTurnEffect {
        Objects.requireNonNull(filter, "filter");
    }

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }

    @Override
    public boolean onlyCardsPutIntoGraveyardFromLibraryThisTurn() {
        return true;
    }
}
