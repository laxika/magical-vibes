package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "WAR", collectorNumber = "7")
public class BulwarkGiant extends Card {

    public BulwarkGiant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(5));
    }
}
