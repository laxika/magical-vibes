package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * Shuffles up to {@code maxTargets} target cards from any graveyards into their owners' libraries.
 * Each selected card may come from a different graveyard unless {@code singleGraveyard} is set
 * ("from a single graveyard", Serene Remembrance). An owner who also has the resolving spell shuffled
 * into their library (a {@link ShuffleIntoLibraryEffect} on the same spell) shuffles only once.
 */
public record ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect(
        CardPredicate filter,
        int maxTargets,
        boolean singleGraveyard
) implements CardEffect {

    public ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect(CardPredicate filter, int maxTargets) {
        this(filter, maxTargets, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                filter == null ? new CardTruePredicate() : filter,
                GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
