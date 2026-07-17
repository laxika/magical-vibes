package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ALA", collectorNumber = "71")
public class Deathgreeter extends Card {

    public Deathgreeter() {
        // Whenever another creature dies, you may gain 1 life.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
