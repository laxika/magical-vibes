package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "M11", collectorNumber = "123")
public class ArcRunner extends Card {

    public ArcRunner() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect());
    }
}
