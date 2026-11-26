package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

public class RemnantOfTheRisingStar extends Card {

    public RemnantOfTheRisingStar() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                UnlessPaysEffect.youMayPayX("{X}", new QueueReflexiveAbilityEffect(
                        new PutCountersEffect(Subjects.triggering(), CounterType.PLUS_ONE_PLUS_ONE, new XValue()))));

        addStaticEffect(new ConditionalEffect(
                new ControlsPermanentCount(5, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsModifiedPredicate()))),
                new StaticBoostEffect(5, 5, Set.of(Keyword.TRAMPLE), Subjects.source())));
    }
}
