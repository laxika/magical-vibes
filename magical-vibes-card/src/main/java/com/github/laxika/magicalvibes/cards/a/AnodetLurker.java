package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "5DN", collectorNumber = "101")
public class AnodetLurker extends Card {

    public AnodetLurker() {
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(3));
    }
}
