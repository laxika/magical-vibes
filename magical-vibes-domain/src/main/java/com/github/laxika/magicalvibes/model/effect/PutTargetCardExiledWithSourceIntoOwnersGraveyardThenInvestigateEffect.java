package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/** Puts the targeted card exiled with the source into its owner's graveyard, then investigates if successful. */
public record PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect(
        CardPredicate filter) implements CardEffect {

    public PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        CardPredicate targetFilter = filter == null ? new CardTruePredicate() : filter;
        return TargetSpec.benign(TargetPredicates.exiledCards(targetFilter));
    }
}
