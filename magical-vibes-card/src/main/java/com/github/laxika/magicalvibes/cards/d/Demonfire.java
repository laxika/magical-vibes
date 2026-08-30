package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "DIS", collectorNumber = "60")
public class Demonfire extends Card {

    public Demonfire() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect(new ControllerHandEmpty()));
        addEffect(EffectSlot.SPELL,
                new DealDamageToAnyTargetEffect(new XValue(), false, true, -1, new ControllerHandEmpty()));
    }
}
