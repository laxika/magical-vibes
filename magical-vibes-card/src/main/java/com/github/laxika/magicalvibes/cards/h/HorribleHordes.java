package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;

@CardRegistration(set = "MIR", collectorNumber = "304")
public class HorribleHordes extends Card {

    public HorribleHordes() {
        // Rampage 1: whenever Horrible Hordes becomes blocked, it gets +1/+1 until end of
        // turn for each creature blocking it beyond the first, i.e. blockers - 1.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RampageEffect(1));
    }
}
