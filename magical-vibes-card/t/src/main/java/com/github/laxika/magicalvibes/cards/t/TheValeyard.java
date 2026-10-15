package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TheValeyardEffect;

@CardRegistration(set = "WHO", collectorNumber = "165")
@CardRegistration(set = "WHO", collectorNumber = "450")
@CardRegistration(set = "WHO", collectorNumber = "770")
@CardRegistration(set = "WHO", collectorNumber = "1041")
public class TheValeyard extends Card {

    public TheValeyard() {
        addEffect(EffectSlot.STATIC, new TheValeyardEffect());
    }
}
