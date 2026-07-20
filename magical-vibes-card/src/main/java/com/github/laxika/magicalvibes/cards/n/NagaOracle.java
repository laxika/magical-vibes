package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "AKH", collectorNumber = "62")
public class NagaOracle extends Card {

    public NagaOracle() {
        // When this creature enters, surveil 3.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(3));
    }
}
