package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;

@CardRegistration(set = "NEO", collectorNumber = "233")
public class RisonaAsariCommander extends Card {

    public RisonaAsariCommander() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.INDESTRUCTIBLE)),
                new PutCountersOnSelfEffect(CounterType.INDESTRUCTIBLE)));

        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU,
                new RemoveCounterFromSourceEffect(CounterType.INDESTRUCTIBLE, 1));
    }
}
