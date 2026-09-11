package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ZNR", collectorNumber = "31")
public class PairedTactician extends Card {

    public PairedTactician() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackingCreaturesOfSubtype(2, CardSubtype.WARRIOR),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
