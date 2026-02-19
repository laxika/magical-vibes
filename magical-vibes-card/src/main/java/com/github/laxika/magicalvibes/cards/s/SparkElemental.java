package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "10E", collectorNumber = "237")
public class SparkElemental extends Card {

    public SparkElemental() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect());
    }
}
