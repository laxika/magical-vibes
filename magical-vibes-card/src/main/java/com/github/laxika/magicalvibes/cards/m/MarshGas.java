package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "4ED", collectorNumber = "146")
public class MarshGas extends Card {

    public MarshGas() {
        // All creatures get -2/-0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, 0));
    }
}
