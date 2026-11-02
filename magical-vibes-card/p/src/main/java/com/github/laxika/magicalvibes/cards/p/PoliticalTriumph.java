package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "31")
public class PoliticalTriumph extends Card {

    public PoliticalTriumph() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        new ScryEffect(1),
                        new PutCountersOnSelfEffect(CounterType.PLAN)));

        addTrigger(Triggers.countersPutOnThis(), ConditionalEffect.atTriggerTime(
                new SourceCounterThreshold(4, CounterType.PLAN),
                SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new DrawCardEffect(),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentIsCreaturePredicate()))));
    }
}
