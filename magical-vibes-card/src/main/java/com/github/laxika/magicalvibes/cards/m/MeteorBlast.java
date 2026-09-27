package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;

@CardRegistration(set = "C15", collectorNumber = "28")
public class MeteorBlast extends Card {

    public MeteorBlast() {
        targetExactlyX(null, 100).addEffect(EffectSlot.SPELL,
                new DealDamageToEachTargetEffect(new Fixed(4)));
    }
}
