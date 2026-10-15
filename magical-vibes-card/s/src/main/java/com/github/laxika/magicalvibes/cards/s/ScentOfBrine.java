package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "UDS", collectorNumber = "45")
public class ScentOfBrine extends Card {

    public ScentOfBrine() {
        addEffect(EffectSlot.SPELL, new RevealAnyNumberOfCardsFromHandEffect(
                new CardColorPredicate(CardColor.BLUE)));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(new EventValue()));
    }
}
