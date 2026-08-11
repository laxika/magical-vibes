package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles every card matching {@code filter} from a target player's hand.
 * Exiling from hand is not discarding, so discard triggers do not fire.
 */
public record ExileMatchingCardsFromTargetPlayerHandEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
