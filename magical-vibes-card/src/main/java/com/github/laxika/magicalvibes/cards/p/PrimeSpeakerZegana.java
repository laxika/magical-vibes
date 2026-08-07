package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "GTC", collectorNumber = "188")
public class PrimeSpeakerZegana extends Card {

    public PrimeSpeakerZegana() {
        // Prime Speaker Zegana enters with X +1/+1 counters on it, where X is the greatest power
        // among other creatures you control. The as-enters replacement runs before the permanent is
        // added to the battlefield, so GreatestPowerAmongControlled already sees only other creatures.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE,
                new GreatestPowerAmongControlled()));

        // When Prime Speaker Zegana enters, draw cards equal to its power.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(new SourcePower()));
    }
}
