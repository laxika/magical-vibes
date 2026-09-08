package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;

@CardRegistration(set = "STX", collectorNumber = "95")
public class CrackleWithPower extends Card {

    public CrackleWithPower() {
        targetX(null, 100).addEffect(EffectSlot.SPELL,
                new DealDamageToEachTargetEffect(new Scaled(new XValue(), 5)));
    }
}
