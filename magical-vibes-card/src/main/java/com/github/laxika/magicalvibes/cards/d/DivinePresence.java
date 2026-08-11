package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdEffect;

@CardRegistration(set = "INV", collectorNumber = "15")
public class DivinePresence extends Card {

    public DivinePresence() {
        addEffect(EffectSlot.STATIC, new ReplaceDamageAboveThresholdEffect(4, 3));
    }
}
