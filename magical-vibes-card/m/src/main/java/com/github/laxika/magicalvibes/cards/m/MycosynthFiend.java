package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.OpponentPoisonCounters;

@CardRegistration(set = "NPH", collectorNumber = "117")
@CardRegistration(set = "ONC", collectorNumber = "109")
public class MycosynthFiend extends Card {

    public MycosynthFiend() {
        // Mycosynth Fiend gets +1/+1 for each poison counter your opponents have.
        addStaticEffect(BoostEffect.self(
                new OpponentPoisonCounters(), new OpponentPoisonCounters()));
    }
}
