package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "67")
@CardRegistration(set = "INR", collectorNumber = "89")
@CardRegistration(set = "ODY", collectorNumber = "103")
@CardRegistration(set = "RTR", collectorNumber = "54")
@CardRegistration(set = "FIN", collectorNumber = "80")
public class Syncopate extends Card {

    public Syncopate() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, true));
    }
}
