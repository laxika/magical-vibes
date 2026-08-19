package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;

@CardRegistration(set = "RIX", collectorNumber = "98")
public class DaringBuccaneer extends Card {

    public DaringBuccaneer() {
        addEffect(EffectSlot.STATIC, new IncreaseOwnCastCostUnlessRevealSubtypeEffect(2, CardSubtype.PIRATE));
    }
}
