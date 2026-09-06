package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CyclingTriggerEffect;

@CardRegistration(set = "IKO", collectorNumber = "129")
public class PricklyMarmoset extends Card {

    public PricklyMarmoset() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new CyclingTriggerEffect(new BoostSelfEffect(2, 0)));
        addCycling("{1}");
    }
}
