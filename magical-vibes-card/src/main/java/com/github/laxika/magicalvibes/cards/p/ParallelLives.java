package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "199")
public class ParallelLives extends Card {

    public ParallelLives() {
        addEffect(EffectSlot.STATIC, new MultiplyTokenCreationEffect(2));
    }
}
