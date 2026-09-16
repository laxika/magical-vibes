package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Creates a token copy of a creature card exiled with the source permanent. */
public record CreateTokenCopyOfExiledCreatureWithSourceEffect(
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect,
        boolean targeted
) implements CardEffect {

    public CreateTokenCopyOfExiledCreatureWithSourceEffect(
            CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        this(tokenCopyEffect, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targeted
                ? TargetSpec.benign(TargetPredicates.exiledCards(new CardTypePredicate(CardType.CREATURE)))
                : TargetSpec.NONE;
    }
}
