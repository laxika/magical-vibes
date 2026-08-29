package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * Shuffles up to {@code maxTargets} target cards from any graveyards into their owners' libraries.
 * Each selected card may come from a different graveyard.
 */
public record ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect(
        CardPredicate filter,
        int maxTargets
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                filter == null ? new CardTruePredicate() : filter,
                GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
