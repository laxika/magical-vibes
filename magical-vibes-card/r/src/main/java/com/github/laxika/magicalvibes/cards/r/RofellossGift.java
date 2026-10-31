package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "UDS", collectorNumber = "119")
public class RofellossGift extends Card {

    public RofellossGift() {
        addSpellEffect(new RevealAnyNumberOfCardsFromHandEffect(new CardColorPredicate(CardColor.GREEN)));
        addSpellEffect(new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), new EventValue(), false));
    }
}
