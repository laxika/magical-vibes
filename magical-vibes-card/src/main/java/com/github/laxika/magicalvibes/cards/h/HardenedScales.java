package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;

@CardRegistration(set = "KTK", collectorNumber = "133")
@CardRegistration(set = "PIP", collectorNumber = "998")
@CardRegistration(set = "2X2", collectorNumber = "151")
@CardRegistration(set = "WOT", collectorNumber = "55")
@CardRegistration(set = "SOC", collectorNumber = "272")
public class HardenedScales extends Card {

    public HardenedScales() {
        addEffect(EffectSlot.STATIC, new AddOnePlusOneCountersEffect());
    }
}
