package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "GTC", collectorNumber = "43")
public class MindeyeDrake extends Card {

    public MindeyeDrake() {
        // When this creature dies, target player mills five cards.
        addEffect(EffectSlot.ON_DEATH, new MillEffect(5, MillRecipient.TARGET_PLAYER));
    }
}
