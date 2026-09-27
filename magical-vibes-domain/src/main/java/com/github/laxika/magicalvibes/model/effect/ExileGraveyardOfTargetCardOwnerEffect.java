package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/** Exiles the entire graveyard that contained the targeted card. */
public record ExileGraveyardOfTargetCardOwnerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.OPPONENT_GRAVEYARD));
    }
}
