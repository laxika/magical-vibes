package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostNonHumanCreaturesByCreatureTypeCountEffect;

@CardRegistration(set = "TLA", collectorNumber = "171")
public class DiligentZookeeper extends Card {

    public DiligentZookeeper() {
        addEffect(EffectSlot.STATIC, new BoostNonHumanCreaturesByCreatureTypeCountEffect(10));
    }
}
