package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "KTK", collectorNumber = "203")
public class SultaiAscendancy extends Card {

    public SultaiAscendancy() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(2));
    }
}
