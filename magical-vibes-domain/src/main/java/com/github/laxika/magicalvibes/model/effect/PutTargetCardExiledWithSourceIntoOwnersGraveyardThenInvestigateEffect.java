package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * Puts a card exiled with the source into its owner's graveyard, then investigates if successful.
 * The card is targeted by default; with {@code chooseAtResolution} the controller chooses it while
 * the ability resolves (Foreboding Steamboat's attack trigger, which does not target).
 */
public record PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect(
        CardPredicate filter, boolean chooseAtResolution) implements CardEffect {

    public PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect() {
        this(null, false);
    }

    public PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect(CardPredicate filter) {
        this(filter, false);
    }

    @Override
    public TargetSpec targetSpec() {
        if (chooseAtResolution) {
            return TargetSpec.NONE;
        }
        CardPredicate targetFilter = filter == null ? new CardTruePredicate() : filter;
        return TargetSpec.benign(TargetPredicates.exiledCards(targetFilter));
    }
}
