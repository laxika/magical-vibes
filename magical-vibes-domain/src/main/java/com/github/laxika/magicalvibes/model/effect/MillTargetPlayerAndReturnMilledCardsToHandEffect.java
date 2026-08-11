package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Mills cards from a target player's library, then returns each matching card milled by this
 * resolution from that player's graveyard to their hand.
 */
public record MillTargetPlayerAndReturnMilledCardsToHandEffect(int count, CardPredicate filter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
