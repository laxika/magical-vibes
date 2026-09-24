package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles up to one matching graveyard card with a discovery counter and grants play permission. */
public record ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffect(
        CardPredicate filter
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS)
                : TargetPredicates.graveyardCards(filter, GraveyardSearchScope.ALL_GRAVEYARDS));
    }

    @Override
    public boolean hasOptionalTarget() {
        return true;
    }
}
