package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "ECL", collectorNumber = "34")
public class ShoreLurker extends Card {

    public ShoreLurker() {
        // When this creature enters, surveil 1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
    }
}
