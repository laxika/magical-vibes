package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * During resolution, choose one matching card from the targeted player's graveyard and exile it.
 * The card choice is non-targeting and uses the resolution-time graveyard choice flow.
 */
public record ExileMatchingCardFromTargetGraveyardEffect(
        CardPredicate filter,
        boolean mayChooseNone,
        boolean trackWithSource) implements CardEffect {

    public ExileMatchingCardFromTargetGraveyardEffect(CardPredicate filter) {
        this(filter, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
