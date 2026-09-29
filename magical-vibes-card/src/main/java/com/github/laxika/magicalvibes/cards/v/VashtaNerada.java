package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "WHO", collectorNumber = "73")
public class VashtaNerada extends Card {

    public VashtaNerada() {
        // Morbid — At the beginning of each end step, if a creature died this turn,
        // put a +1/+1 counter on this creature.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new Morbid(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
