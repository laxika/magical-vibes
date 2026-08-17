package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "GRN", collectorNumber = "55")
public class ThoughtboundPhantasm extends Card {

    public ThoughtboundPhantasm() {
        addEffect(EffectSlot.ON_CONTROLLER_SURVEILS,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE),
                new CanAttackAsThoughNoDefenderEffect()));
    }
}
