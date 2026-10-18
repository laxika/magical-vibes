package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "ISD", collectorNumber = "202")
public class SomberwaldSpider extends Card {

    public SomberwaldSpider() {
        // Morbid — enters with two +1/+1 counters if a creature died this turn (CR 614.1c, as-enters replacement).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Morbid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
    }
}
