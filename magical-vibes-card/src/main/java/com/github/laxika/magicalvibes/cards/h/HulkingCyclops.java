package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "POR", collectorNumber = "134")
@CardRegistration(set = "VIS", collectorNumber = "84")
@CardRegistration(set = "6ED", collectorNumber = "189")
@CardRegistration(set = "8ED", collectorNumber = "195")
@CardRegistration(set = "BTD", collectorNumber = "38")
public class HulkingCyclops extends Card {

    public HulkingCyclops() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
