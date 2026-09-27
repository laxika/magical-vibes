package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static effect that grants a card type to each nonland permanent controlled by the source's
 * controller and, optionally, to that player's nonland permanent cards outside the battlefield.
 *
 * @param cardType the card type to grant
 * @param includeNonBattlefieldCards whether nonland permanent cards outside the battlefield also
 *                                   receive the grant
 */
public record GrantCardTypeToOwnNonlandPermanentsEffect(
        CardType cardType,
        boolean includeNonBattlefieldCards
) implements CardEffect {

    public GrantCardTypeToOwnNonlandPermanentsEffect(CardType cardType) {
        this(cardType, true);
    }
}
