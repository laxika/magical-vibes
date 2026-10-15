package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MH1", collectorNumber = "153")
public class ViashinoSandsprinter extends Card {

    public ViashinoSandsprinter() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, ReturnToHandEffect.self());
        addCycling("{R}");
    }
}
