package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * During resolution, choose one matching card from the targeted player's graveyard and exile it.
 * The card choice is non-targeting and uses the resolution-time graveyard choice flow.
 */
public record ExileMatchingCardFromTargetGraveyardEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
