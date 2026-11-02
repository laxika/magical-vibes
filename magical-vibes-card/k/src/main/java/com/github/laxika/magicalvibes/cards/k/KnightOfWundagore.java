package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "175")
public class KnightOfWundagore extends Card {

    public KnightOfWundagore() {
        addTrigger(Triggers.youPutPlusOneCountersOnAnotherCreature(),
                new OncePerTurnTriggerEffect(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
