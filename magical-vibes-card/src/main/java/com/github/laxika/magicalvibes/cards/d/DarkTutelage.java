package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;

@CardRegistration(set = "M11", collectorNumber = "90")
public class DarkTutelage extends Card {

    public DarkTutelage() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RevealTopCardPutIntoHandAndLoseLifeEffect());
    }
}
