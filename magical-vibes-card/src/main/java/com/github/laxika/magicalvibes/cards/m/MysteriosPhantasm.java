package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "SPM", collectorNumber = "38")
public class MysteriosPhantasm extends Card {

    public MysteriosPhantasm() {
        addEffect(EffectSlot.ON_ATTACK, new MillEffect(1, MillRecipient.CONTROLLER));
    }
}
