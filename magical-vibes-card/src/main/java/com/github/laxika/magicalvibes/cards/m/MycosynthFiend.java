package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOpponentPoisonCounterEffect;

@CardRegistration(set = "NPH", collectorNumber = "117")
public class MycosynthFiend extends Card {

    public MycosynthFiend() {
        addEffect(EffectSlot.STATIC, new BoostSelfPerOpponentPoisonCounterEffect(1, 1));
    }
}
