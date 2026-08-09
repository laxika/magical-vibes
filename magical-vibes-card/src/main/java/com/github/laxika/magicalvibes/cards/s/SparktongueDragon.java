package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "M19", collectorNumber = "159")
public class SparktongueDragon extends Card {

    public SparktongueDragon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayPayManaEffect(
                "{2}{R}",
                new DealDamageToAnyTargetEffect(3),
                "Pay {2}{R} to deal 3 damage to any target?"
        ));
    }
}
