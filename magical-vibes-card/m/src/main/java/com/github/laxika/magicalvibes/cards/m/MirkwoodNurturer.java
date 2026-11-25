package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "160")
public class MirkwoodNurturer extends Card {

    public MirkwoodNurturer() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                "Target must be another permanent you control"
        ), 0, 1).addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ReturnToHandEffect.target(),
                new ConditionalEffect(HappenedThisWay.returned(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
