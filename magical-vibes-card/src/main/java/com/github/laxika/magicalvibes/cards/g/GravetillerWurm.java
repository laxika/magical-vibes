package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "DKA", collectorNumber = "116")
public class GravetillerWurm extends Card {

    public GravetillerWurm() {
        // Morbid - Gravetiller Wurm enters the battlefield with four +1/+1 counters on it
        // if a creature died this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Morbid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4))));
    }
}
