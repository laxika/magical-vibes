package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerEffect;

@CardRegistration(set = "STX", collectorNumber = "130")
public class ExponentialGrowth extends Card {

    public ExponentialGrowth() {
        addEffect(EffectSlot.SPELL, new DoubleTargetCreaturePowerEffect(new XValue()));
    }
}
