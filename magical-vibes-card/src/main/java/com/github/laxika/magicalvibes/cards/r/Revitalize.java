package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M19", collectorNumber = "35")
@CardRegistration(set = "M21", collectorNumber = "31")
@CardRegistration(set = "KHM", collectorNumber = "23")
public class Revitalize extends Card {

    public Revitalize() {
        // You gain 3 life.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));

        // Draw a card.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
