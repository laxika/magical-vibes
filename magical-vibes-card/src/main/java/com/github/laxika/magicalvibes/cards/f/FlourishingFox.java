package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CyclingTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "IKO", collectorNumber = "13")
public class FlourishingFox extends Card {

    public FlourishingFox() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new CyclingTriggerEffect(new PutCountersOnSourceEffect(1, 1, 1)));
        addCycling("{1}");
    }
}
