package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;

@CardRegistration(set = "FUT", collectorNumber = "164")
public class SoultetherGolem extends Card {

    public SoultetherGolem() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(1)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.TIME));
    }
}
