package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisCombat;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceAtEndOfCombatEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "155")
public class ClockworkDragon extends Card {

    public ClockworkDragon() {
        // This creature enters with six +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(6)));

        // At end of combat, if this creature attacked or blocked this combat, remove a +1/+1
        // counter from it. The combat condition is checked as the end-of-combat trigger fires.
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new SourceAttackedOrBlockedThisCombat(),
                        new RemoveCounterFromSourceAtEndOfCombatEffect(CounterType.PLUS_ONE_PLUS_ONE)));

        // {3}: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{3}: Put a +1/+1 counter on this creature."));
    }
}
