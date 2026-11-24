package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "94")
public class CrazedFirecat extends Card {

    public CrazedFirecat() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), FlipUntilLoseEffect.untilLose(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new AmountThisWay(StepKind.FLIP)));
    }
}
