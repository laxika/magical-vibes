package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Shuffles a graveyard into its owner's library. {@code filter} narrows which cards make the trip
 * ("shuffle all creature cards from your graveyard into your library", Barishi); {@code null} moves
 * the whole graveyard.
 */
public record ShuffleGraveyardIntoLibraryEffect(boolean targetPlayer, CardPredicate filter) implements CardEffect {

    public ShuffleGraveyardIntoLibraryEffect(boolean targetPlayer) {
        this(targetPlayer, null);
    }

    @Override public TargetSpec targetSpec() {
        return targetPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
