package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "5DN", collectorNumber = "27")
public class Condescend extends Card {

    public Condescend() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, false));
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
    }
}
