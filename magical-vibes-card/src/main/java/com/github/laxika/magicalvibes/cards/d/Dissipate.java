package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "53")
public class Dissipate extends Card {

    public Dissipate() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndExileEffect());
    }
}
