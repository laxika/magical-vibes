package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;

@CardRegistration(set = "UDS", collectorNumber = "17")
public class ScentOfJasmine extends Card {

    public ScentOfJasmine() {
        addSpellEffect(new RevealAnyNumberOfCardsFromHandEffect(
                new CardColorPredicate(CardColor.WHITE)));
        addSpellEffect(new GainLifeEffect(new Scaled(
                new EventValue(),
                2
        )));
    }
}
