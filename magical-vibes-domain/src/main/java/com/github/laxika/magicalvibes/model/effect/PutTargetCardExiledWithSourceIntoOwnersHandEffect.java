package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/** Puts the targeted card exiled with the source permanent into its owner's hand. */
public record PutTargetCardExiledWithSourceIntoOwnersHandEffect(CardPredicate filter) implements CardEffect {

    public PutTargetCardExiledWithSourceIntoOwnersHandEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        CardPredicate targetFilter = filter == null ? new CardTruePredicate() : filter;
        return TargetSpec.benign(TargetPredicates.exiledCards(targetFilter));
    }
}
