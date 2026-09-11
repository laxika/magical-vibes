package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

/**
 * Reveals the first card drawn each turn and draws an additional card when that card is a
 * creature.
 */
public record RevealFirstDrawCreatureEffect() implements FirstDrawRevealTriggerEffect {

    @Override
    public CardEffect effectFor(Card drawnCard) {
        return drawnCard.hasType(CardType.CREATURE) ? new DrawCardEffect(1) : null;
    }
}
