package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "M14", collectorNumber = "200")
public class VoraciousWurm extends Card {

    public VoraciousWurm() {
        // This creature enters with X +1/+1 counters on it, where X is the amount of life you've
        // gained this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE,
                new LifeGainedThisTurn(CountScope.CONTROLLER)));
    }
}
