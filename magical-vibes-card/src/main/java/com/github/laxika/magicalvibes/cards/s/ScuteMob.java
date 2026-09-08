package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ZEN", collectorNumber = "182")
public class ScuteMob extends Card {

    public ScuteMob() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new ControlsPermanentCount(5, new PermanentIsLandPredicate()),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 4)));
    }
}
