package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromManaValueParityEffect;

@CardRegistration(set = "IKO", collectorNumber = "228")
public class OboshThePreypiercer extends Card {

    public OboshThePreypiercer() {
        addEffect(EffectSlot.STATIC, new DoubleDamageFromManaValueParityEffect(ManaValueParity.ODD));
    }
}
