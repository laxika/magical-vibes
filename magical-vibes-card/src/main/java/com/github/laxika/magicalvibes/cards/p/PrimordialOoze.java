package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddCounterThenPayCountersOrTapAndDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "5ED", collectorNumber = "261")
public class PrimordialOoze extends Card {

    public PrimordialOoze() {
        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // At the beginning of your upkeep, put a +1/+1 counter on this creature. Then you may pay
        // {X}, where X is the number of +1/+1 counters on it. If you don't, tap this creature and
        // it deals X damage to you.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new AddCounterThenPayCountersOrTapAndDamageEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
