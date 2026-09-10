package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

/**
 * Draw-trigger marker that reveals every card drawn and selects a land or nonland effect.
 */
public record RevealEachDrawEffect(CardEffect onLand, CardEffect onNonland)
        implements DrawRevealTriggerEffect {

    @Override
    public CardEffect effectFor(Card drawnCard) {
        return drawnCard.hasType(CardType.LAND) ? onLand : onNonland;
    }
}
