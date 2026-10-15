package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MIR", collectorNumber = "286")
public class VitalizingCascade extends Card {

    public VitalizingCascade() {
        // You gain X plus 3 life.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Sum(new XValue(), new Fixed(3))));
    }
}
