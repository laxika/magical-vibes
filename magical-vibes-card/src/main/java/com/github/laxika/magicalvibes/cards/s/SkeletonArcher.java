package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M19", collectorNumber = "118")
@CardRegistration(set = "M20", collectorNumber = "324")
@CardRegistration(set = "M21", collectorNumber = "123")
public class SkeletonArcher extends Card {

    public SkeletonArcher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(1));
    }
}
