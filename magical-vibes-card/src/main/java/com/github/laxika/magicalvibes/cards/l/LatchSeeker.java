package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "AVR", collectorNumber = "63")
public class LatchSeeker extends Card {

    public LatchSeeker() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
