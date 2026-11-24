package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "175")
public class GallopingLizrog extends Card {

    public GallopingLizrog() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                RemoveUpToCountersFromAllPermanentsEffect.anyNumber(CounterType.PLUS_ONE_PLUS_ONE,
                        Subjects.creaturesYouControl()),
                new ConditionalEffect(HappenedThisWay.removedCounters(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                                new Scaled(new AmountThisWay(StepKind.REMOVE_COUNTERS), 2)))));
    }
}
