package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;

@CardRegistration(set = "10E", collectorNumber = "246")
public class ViashinoSandscout extends Card {

    public ViashinoSandscout() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ReturnSelfToHandEffect());
    }
}
