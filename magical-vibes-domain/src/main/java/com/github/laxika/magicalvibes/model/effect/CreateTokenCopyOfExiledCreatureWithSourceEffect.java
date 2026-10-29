package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Creates a token copy of a creature card exiled with the source permanent. With {@code anyExiledCard}
 * the exiled card need not be a creature card (e.g. a land that was animated when it was exiled).
 */
public record CreateTokenCopyOfExiledCreatureWithSourceEffect(
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect,
        boolean targeted,
        boolean anyExiledCard
) implements CardEffect {

    public CreateTokenCopyOfExiledCreatureWithSourceEffect(
            CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect, boolean targeted) {
        this(tokenCopyEffect, targeted, false);
    }

    public CreateTokenCopyOfExiledCreatureWithSourceEffect(
            CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        this(tokenCopyEffect, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targeted
                ? TargetSpec.benign(TargetPredicates.exiledCards(new CardTypePredicate(CardType.CREATURE)))
                : TargetSpec.NONE;
    }
}
