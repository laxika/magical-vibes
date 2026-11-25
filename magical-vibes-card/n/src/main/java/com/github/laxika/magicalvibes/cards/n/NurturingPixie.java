package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "20")
public class NurturingPixie extends Card {

    public NurturingPixie() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.FAERIE)),
                        new PermanentNotPredicate(new PermanentIsLandPredicate())
                )),
                "Target must be a non-Faerie, nonland permanent you control"
        ), 0, 1).addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(ReturnToHandEffect.target(),
                        new ConditionalEffect(HappenedThisWay.returned(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
