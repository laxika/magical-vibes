package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "DSK", collectorNumber = "180")
public class GraspingLongneck extends Card {

    public GraspingLongneck() {
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(2));
    }
}
