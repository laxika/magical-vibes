package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M10", collectorNumber = "158")
public class SparkmageApprentice extends Card {

    public SparkmageApprentice() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(1));
    }
}
