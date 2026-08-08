package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M19", collectorNumber = "13")
public class HeraldOfFaith extends Card {

    public HeraldOfFaith() {
        // Whenever this creature attacks, you gain 2 life.
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(2));
    }
}
