package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "ODY", collectorNumber = "104")
public class ThinkTank extends Card {

    public ThinkTank() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(1));
    }
}
