package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * Exiles a target card from any graveyard and, if that succeeds, puts a +1/+1 counter on the
 * source creature.
 */
public record ExileTargetCardFromGraveyardPutCounterOnSourceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
