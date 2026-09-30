package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * Registers a delayed trigger that returns a matching card from its owner's graveyard to the
 * battlefield under the ability controller's control at the beginning of the next end step.
 *
 * <p>The card id is bound by the trigger collector for events that can involve multiple cards.
 */
public record RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(
        CardPredicate filter,
        UUID cardId
) implements CardEffect, SacrificedPermanentCardAwareEffect {

    public RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(CardPredicate filter) {
        this(filter, null);
    }

    @Override
    public CardEffect boundToSacrificedPermanent(Card sacrificedCard) {
        return new RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(filter, sacrificedCard.getId());
    }
}
