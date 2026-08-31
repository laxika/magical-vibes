package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Puts a targeted face-up exiled card on the bottom of its owner's library. */
public record PutTargetCardFromExileOnBottomOfOwnersLibraryEffect(
        CardPredicate filter,
        boolean notOwnedOnly
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exileCard());
    }
}
