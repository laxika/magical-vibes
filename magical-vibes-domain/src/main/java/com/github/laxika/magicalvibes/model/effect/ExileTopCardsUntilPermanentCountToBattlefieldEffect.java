package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Exiles cards from the top of the controller's library until enough permanent cards are found. */
public record ExileTopCardsUntilPermanentCountToBattlefieldEffect(DynamicAmount permanentCount)
        implements CardEffect {
}
