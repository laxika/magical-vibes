package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At resolution, optionally exiles any number of matching cards from the controller's graveyard.
 * If at least one card is exiled, the supplied effect is put onto the stack as a reflexive ability.
 */
public record ExileAnyNumberOfOwnGraveyardCardsThenEffect(CardPredicate exileFilter,
                                                           CardEffect thenEffect)
        implements CardEffect {
}
