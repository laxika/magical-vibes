package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "15")
public class GuardianKirin extends Card {

    public GuardianKirin() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
