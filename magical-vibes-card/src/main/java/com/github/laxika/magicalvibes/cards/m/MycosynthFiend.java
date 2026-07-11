package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentPoisonCounters;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "NPH", collectorNumber = "117")
public class MycosynthFiend extends Card {

    public MycosynthFiend() {
        // Mycosynth Fiend gets +1/+1 for each poison counter your opponents have.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new OpponentPoisonCounters(), new OpponentPoisonCounters()));
    }
}
