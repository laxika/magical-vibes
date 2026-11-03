package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasKeyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2085")
public class MilesTailsPrower extends Card {

    public MilesTailsPrower() {
        TriggeringPermanentHasKeyword hasFlying = new TriggeringPermanentHasKeyword(Keyword.FLYING);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ARTIFACT,
                                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        new ConditionalEffect(hasFlying, new DrawCardEffect(1)),
                        new ConditionalEffect(new NotCondition(hasFlying),
                                new PutCounterOnReferencedPermanentEffect(
                                        PermanentReference.TRIGGERING, CounterType.FLYING))));
    }
}
