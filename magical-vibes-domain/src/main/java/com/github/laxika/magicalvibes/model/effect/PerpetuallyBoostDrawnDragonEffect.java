package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;

/** Draw trigger for "Whenever you draw a Dragon card, it perpetually gets +1/+1." */
public record PerpetuallyBoostDrawnDragonEffect() implements DrawnCardTriggerEffect {

    @Override
    public CardEffect effectForDrawnCard(Card drawnCard) {
        return drawnCard.getSubtypes().contains(CardSubtype.DRAGON)
                ? new PerpetuallyBoostCardEffect(drawnCard, 1, 1)
                : null;
    }
}
