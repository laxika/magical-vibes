package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "M20", collectorNumber = "195")
public class SilverbackShaman extends Card {

    public SilverbackShaman() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
