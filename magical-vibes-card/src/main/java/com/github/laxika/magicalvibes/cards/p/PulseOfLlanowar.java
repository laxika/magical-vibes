package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReplaceBasicLandManaWithAnyColorEffect;

@CardRegistration(set = "INV", collectorNumber = "202")
public class PulseOfLlanowar extends Card {

    public PulseOfLlanowar() {
        addEffect(EffectSlot.STATIC, new ReplaceBasicLandManaWithAnyColorEffect());
    }
}
