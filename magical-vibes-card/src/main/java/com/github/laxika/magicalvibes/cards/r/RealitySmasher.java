package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;

@CardRegistration(set = "OGW", collectorNumber = "7")
public class RealitySmasher extends Card {

    public RealitySmasher() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessDiscardsEffect());
    }
}
