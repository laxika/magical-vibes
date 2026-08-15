package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "M20", collectorNumber = "55")
@CardRegistration(set = "EMN", collectorNumber = "53")
public class Convolute extends Card {

    public Convolute() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(4));
    }
}
