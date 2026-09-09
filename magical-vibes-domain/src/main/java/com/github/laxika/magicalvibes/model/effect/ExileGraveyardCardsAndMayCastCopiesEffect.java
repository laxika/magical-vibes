package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles targeted graveyard cards, creates copies, and offers those copies for casting. */
public record ExileGraveyardCardsAndMayCastCopiesEffect(
        CardPredicate filter,
        GraveyardSearchScope scope
) implements GraveyardCardChoosingEffect {

    @Override
    public int graveyardChoiceMaxTargets() {
        return Integer.MAX_VALUE;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return filter;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}
