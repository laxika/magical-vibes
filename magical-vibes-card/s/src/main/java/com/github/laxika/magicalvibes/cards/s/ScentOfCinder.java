package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "UDS", collectorNumber = "96")
public class ScentOfCinder extends Card {

    public ScentOfCinder() {
        addSpellEffect(new RevealAnyNumberOfCardsFromHandEffect(
                new CardColorPredicate(CardColor.RED)));
        addSpellEffect(new DealDamageToAnyTargetEffect(new EventValue()));
    }
}
