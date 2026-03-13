package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealRandomHandCardAndPlayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "160")
public class WildEvocation extends Card {

    public WildEvocation() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new RevealRandomHandCardAndPlayEffect());
    }
}
