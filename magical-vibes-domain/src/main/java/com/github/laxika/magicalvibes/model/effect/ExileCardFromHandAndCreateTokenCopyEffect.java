package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Lets a player exile a matching card from hand and optionally create a token copy of that card. */
public record ExileCardFromHandAndCreateTokenCopyEffect(
        CardPredicate filter,
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect,
        CardPredicate copyFilter,
        boolean targetPlayer
) implements CardEffect {

    public ExileCardFromHandAndCreateTokenCopyEffect(
            CardPredicate filter, CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        this(filter, tokenCopyEffect, null, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
