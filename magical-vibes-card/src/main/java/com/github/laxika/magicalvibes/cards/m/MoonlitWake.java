package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MMQ", collectorNumber = "29")
public class MoonlitWake extends Card {

    public MoonlitWake() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new GainLifeEffect(1));
    }
}
