package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/** Puts the targeted card exiled with the source permanent into its owner's graveyard. */
public record PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect(
        CardPredicate filter, boolean requiresManaValueEqualsX) implements CardEffect {

    public PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect() {
        this(null, false);
    }

    public PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect(CardPredicate filter) {
        this(filter, false);
    }

    @Override
    public TargetSpec targetSpec() {
        CardPredicate targetFilter = filter == null ? new CardTruePredicate() : filter;
        return TargetSpec.benign(TargetPredicates.exiledCards(targetFilter));
    }
}
