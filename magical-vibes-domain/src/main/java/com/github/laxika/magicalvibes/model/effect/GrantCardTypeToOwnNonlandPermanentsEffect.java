package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static effect that grants a card type to each nonland permanent controlled by the source's
 * controller and to that player's nonland permanent cards outside the battlefield.
 *
 * @param cardType the card type to grant
 */
public record GrantCardTypeToOwnNonlandPermanentsEffect(CardType cardType) implements CardEffect {
}
