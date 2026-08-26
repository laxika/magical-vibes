package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "PLC", collectorNumber = "25")
public class ManaTithe extends Card {

    public ManaTithe() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(1));
    }
}
