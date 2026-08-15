package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;

@CardRegistration(set = "WWK", collectorNumber = "76")
public class CometStorm extends Card {

    public CometStorm() {
        setAdditionalCostPerExtraTarget(1);
        target(1, 100).addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new XValue()));
    }
}
