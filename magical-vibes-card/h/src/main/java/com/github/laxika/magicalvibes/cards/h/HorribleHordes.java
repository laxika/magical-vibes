package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "304")
public class HorribleHordes extends Card {

    public HorribleHordes() {
        // Rampage 1: whenever Horrible Hordes becomes blocked, it gets +1/+1 until end of
        // turn for each creature blocking it beyond the first, i.e. blockers - 1.
        addTrigger(Triggers.becomesBlocked(), new RampageEffect(1));
    }
}
