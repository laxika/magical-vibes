package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "8ED", collectorNumber = "68")
public class Concentrate extends Card {

    public Concentrate() {
        // Draw three cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
