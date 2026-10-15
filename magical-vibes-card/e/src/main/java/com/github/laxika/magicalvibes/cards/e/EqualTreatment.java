package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdThisTurnEffect;

@CardRegistration(set = "TOR", collectorNumber = "4")
public class EqualTreatment extends Card {

    public EqualTreatment() {
        addEffect(EffectSlot.SPELL, new ReplaceDamageAboveThresholdThisTurnEffect(1, 2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
