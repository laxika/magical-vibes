package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * Exiles the targeted card from a graveyard and, if it was a creature card, puts a +1/+1 counter
 * on the targeted creature the ability's controller controls.
 */
public record ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect(
        int graveyardTargetGroup, int creatureTargetGroup) implements CardEffect {

    public ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
