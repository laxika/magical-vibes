package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "GTC", collectorNumber = "199")
public class SparkTrooper extends Card {

    public SparkTrooper() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect());
    }
}
