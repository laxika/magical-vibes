package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSurveilledThisTurnPredicate;

/**
 * Static permission to play lands and cast spells from among cards surveilled this turn, paying
 * life equal to a spell's mana value instead of its mana cost.
 */
public record CastSurveilledCardsFromGraveyardByPayingLifeEffect()
        implements CastSpellsFromGraveyardPermission, PlayLandsFromGraveyardPermission {

    private static final CardPredicate FILTER = new CardSurveilledThisTurnPredicate();

    @Override
    public CardPredicate filter() {
        return FILTER;
    }

    @Override
    public CardPredicate landFilter() {
        return FILTER;
    }

    @Override
    public CostEffect alternateCost() {
        return new PayLifeEqualToSpellManaValueCost();
    }

    @Override
    public boolean permitsLandPlayFromGraveyard() {
        return true;
    }
}
