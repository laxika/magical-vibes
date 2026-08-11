package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;

@CardRegistration(set = "KTK", collectorNumber = "133")
public class HardenedScales extends Card {

    public HardenedScales() {
        addEffect(EffectSlot.STATIC, new AddOnePlusOneCountersEffect());
    }
}
