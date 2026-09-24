package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At resolution, optionally exile one matching card from the controller's graveyard. If a card
 * is exiled, the supplied effect is put onto the stack as a reflexive triggered ability.
 *
 * <p>If the follow-up effect requires a graveyard target, that target is chosen only after the
 * exile succeeds, as the reflexive ability goes on the stack.</p>
 *
 * <p>When {@code mandatory} is true, the choice must contain one card whenever a matching card
 * is available.</p>
 */
public record ExileOwnGraveyardCardThenEffect(CardPredicate exileFilter, CardEffect thenEffect,
                                               boolean trackWithSource, boolean mandatory)
        implements CardEffect {

    public ExileOwnGraveyardCardThenEffect(CardPredicate exileFilter, CardEffect thenEffect) {
        this(exileFilter, thenEffect, false, false);
    }

    public ExileOwnGraveyardCardThenEffect(CardPredicate exileFilter, CardEffect thenEffect,
                                           boolean trackWithSource) {
        this(exileFilter, thenEffect, trackWithSource, false);
    }
}
