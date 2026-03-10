package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "M11", collectorNumber = "2")
public class AjanisMantra extends Card {

    public AjanisMantra() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new GainLifeEffect(1),
                "Gain 1 life?"
        ));
    }
}
