package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkersWithLoyaltyBecomeCreaturesEffect;

@CardRegistration(set = "MAT", collectorNumber = "5")
public class SparkRupture extends Card {

    public SparkRupture() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.STATIC, new PlaneswalkersWithLoyaltyBecomeCreaturesEffect());
    }
}
