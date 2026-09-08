package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/** Moves a targeted face-up exiled card matching the filter to its owner's library bottom. */
public record PutTargetExiledCardOnBottomOfOwnersLibraryEffect(CardPredicate filter)
        implements CardEffect {

    public PutTargetExiledCardOnBottomOfOwnersLibraryEffect() {
        this(new CardTruePredicate());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exiledCards(filter));
    }
}
