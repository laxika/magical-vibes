package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReverseTurnOrderEffect;

@CardRegistration(set = "OPC2", collectorNumber = "8")
public class TimeDistortion extends Card {

    public TimeDistortion() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new ReverseTurnOrderEffect());
    }
}
