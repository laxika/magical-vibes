package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "25")
public class Anthroplasm extends Card {

    public Anthroplasm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new RemoveAllCountersEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())
                ),
                "{X}, {T}: Remove all +1/+1 counters from this creature and put X +1/+1 counters on it."
        ));
    }
}
