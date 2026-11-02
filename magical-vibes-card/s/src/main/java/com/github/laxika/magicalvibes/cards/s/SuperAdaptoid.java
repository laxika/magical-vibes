package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "250")
public class SuperAdaptoid extends Card {

    public SuperAdaptoid() {
        PermanentCount legendaryCreaturesYouControl = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(legendaryCreaturesYouControl, new Fixed(2)));

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        target(new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.HASTE, CounterType.HASTE))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.FLYING, CounterType.FLYING))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.FIRST_STRIKE, CounterType.FIRST_STRIKE))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.DOUBLE_STRIKE, CounterType.DOUBLE_STRIKE))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.DEATHTOUCH, CounterType.DEATHTOUCH))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.INDESTRUCTIBLE, CounterType.INDESTRUCTIBLE))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.LIFELINK, CounterType.LIFELINK))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.MENACE, CounterType.MENACE))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.REACH, CounterType.REACH))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.TRAMPLE, CounterType.TRAMPLE))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        copyKeywordAsCounter(Keyword.VIGILANCE, CounterType.VIGILANCE))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.HASTE, CounterType.HASTE))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.FLYING, CounterType.FLYING))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.FIRST_STRIKE, CounterType.FIRST_STRIKE))
                .addTrigger(Triggers.attacks(),
                        copyKeywordAsCounter(Keyword.DOUBLE_STRIKE, CounterType.DOUBLE_STRIKE))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.DEATHTOUCH, CounterType.DEATHTOUCH))
                .addTrigger(Triggers.attacks(),
                        copyKeywordAsCounter(Keyword.INDESTRUCTIBLE, CounterType.INDESTRUCTIBLE))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.LIFELINK, CounterType.LIFELINK))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.MENACE, CounterType.MENACE))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.REACH, CounterType.REACH))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.TRAMPLE, CounterType.TRAMPLE))
                .addTrigger(Triggers.attacks(), copyKeywordAsCounter(Keyword.VIGILANCE, CounterType.VIGILANCE));
    }

    private static CardEffect copyKeywordAsCounter(Keyword keyword, CounterType counterType) {
        return new ConditionalEffect(
                new TargetPermanentMatches(new PermanentHasKeywordPredicate(keyword)),
                new ConditionalEffect(new NotCondition(new SelfHasKeyword(keyword)),
                        PutCountersOnSelfEffect.targeted(counterType), false), false);
    }
}
