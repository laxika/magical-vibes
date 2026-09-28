package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOnePlusOneCountersEffect;

@CardRegistration(set = "LCC", collectorNumber = "234")
public class BranchingEvolution extends Card {

    public BranchingEvolution() {
        // If one or more +1/+1 counters would be put on a creature you control, twice that many
        // +1/+1 counters are put on it instead.
        addEffect(EffectSlot.STATIC, new DoublePlusOnePlusOneCountersEffect());
    }
}
