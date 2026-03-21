package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "67")
public class Syncopate extends Card {

    public Syncopate() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, true));
    }
}
