package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * Static permission to play one land and cast one spell each controller turn from any graveyard,
 * provided the card was put there from a library during that turn.
 */
public record PlayLandAndCastSpellFromCardsPutIntoGraveyardsFromLibrariesThisTurnEffect()
        implements CastSpellsFromGraveyardPermission, PlayLandsFromGraveyardPermission {

    @Override
    public CardPredicate filter() {
        return new CardTruePredicate();
    }

    @Override
    public boolean oncePerControllerTurnForLand() {
        return true;
    }

    @Override
    public boolean oncePerControllerTurnForSpell() {
        return true;
    }

    @Override
    public boolean permitsLandPlayFromGraveyard() {
        return true;
    }

    @Override
    public boolean onlyDuringControllerTurn() {
        return true;
    }

    @Override
    public GraveyardSearchScope graveyardScope() {
        return GraveyardSearchScope.ALL_GRAVEYARDS;
    }

    @Override
    public boolean onlyCardsPutIntoGraveyardFromLibraryThisTurn() {
        return true;
    }
}
