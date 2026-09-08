package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "PLC", collectorNumber = "149")
public class Harmonize extends Card {

    public Harmonize() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
