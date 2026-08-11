package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;

@CardRegistration(set = "ECL", collectorNumber = "182")
public class MidnightTilling extends Card {

    public MidnightTilling() {
        addEffect(EffectSlot.SPELL, new MillControllerAndMayReturnMilledPermanentToHandEffect(4));
    }
}
