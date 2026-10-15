package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "DFT", collectorNumber = "60")
public class ScroungingSkyray extends Card {

    public ScroungingSkyray() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
