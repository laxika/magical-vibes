package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "M14", collectorNumber = "198")
public class VastwoodHydra extends Card {

    public VastwoodHydra() {
        // This creature enters with X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        // When this creature dies, you may distribute a number of +1/+1 counters equal to the number
        // of +1/+1 counters on this creature among any number of creatures you control.
        // Does not target — division rides on pendingETBDamageAssignments at resolution.
        addEffect(EffectSlot.ON_DEATH,
                DistributeCountersAmongCreaturesOnDeathEffect
                        .fromDyingSourceCountersAmongControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
