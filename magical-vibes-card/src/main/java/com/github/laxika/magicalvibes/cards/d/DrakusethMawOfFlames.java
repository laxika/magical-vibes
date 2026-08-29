package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;

@CardRegistration(set = "M20", collectorNumber = "136")
@CardRegistration(set = "FDN", collectorNumber = "193")
public class DrakusethMawOfFlames extends Card {

    public DrakusethMawOfFlames() {
        target(1, 1).addEffect(EffectSlot.ON_ATTACK,
                DealDamageToAnyTargetEffect.forTargetGroup(4, 0));
        target(0, 2).addEffect(EffectSlot.ON_ATTACK,
                new DealDamageToEachTargetEffect(new Fixed(3)));
    }
}
