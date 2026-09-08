package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles one matching card from a graveyard, then gives the controller a fixed amount of life.
 */
public record ExileTargetCardFromGraveyardAndGainLifeEffect(CardPredicate filter, int lifeGain)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS)
                : TargetPredicates.graveyardCards(filter, GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
