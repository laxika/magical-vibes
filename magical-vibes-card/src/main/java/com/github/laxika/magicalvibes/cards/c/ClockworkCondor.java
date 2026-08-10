package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceAtEndOfCombatEffect;

@CardRegistration(set = "MRD", collectorNumber = "154")
public class ClockworkCondor extends Card {

    public ClockworkCondor() {
        // This creature enters with three +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));

        // Whenever this creature attacks or blocks, remove a +1/+1 counter from it at end of combat.
        addEffect(EffectSlot.ON_ATTACK,
                new RemoveCounterFromSourceAtEndOfCombatEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.ON_BLOCK,
                new RemoveCounterFromSourceAtEndOfCombatEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
