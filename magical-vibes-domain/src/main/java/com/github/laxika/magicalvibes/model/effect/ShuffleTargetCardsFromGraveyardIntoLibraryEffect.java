package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Target player shuffles up to maxTargets target cards from their graveyard into their library.
 * The target player is chosen at cast time (canTargetPlayer), then graveyard card selection is prompted.
 * Resolution: moves selected cards from graveyard to library, then shuffles.
 */
public record ShuffleTargetCardsFromGraveyardIntoLibraryEffect(
        CardPredicate filter,
        int maxTargets
) implements GraveyardCardChoosingEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.player()); }

    @Override public int graveyardChoiceMaxTargets() { return maxTargets; }

    @Override public CardPredicate graveyardChoiceFilter() { return filter; }
}
