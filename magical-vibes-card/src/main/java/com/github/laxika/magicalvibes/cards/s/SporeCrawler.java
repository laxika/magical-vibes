package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "INR", collectorNumber = "218")
public class SporeCrawler extends Card {

    public SporeCrawler() {
        // When this creature dies, draw a card.
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
