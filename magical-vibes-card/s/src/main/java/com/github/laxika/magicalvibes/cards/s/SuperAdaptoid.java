package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "250")
public class SuperAdaptoid extends Card {

    public SuperAdaptoid() {
        PermanentCount legendaryCreaturesYouControl = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(legendaryCreaturesYouControl, new Fixed(2)));

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        target(new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, copyKeywordAsCounter(Keyword.HASTE, CounterType.HASTE))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, copyKeywordAsCounter(Keyword.FLYING, CounterType.FLYING))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        copyKeywordAsCounter(Keyword.FIRST_STRIKE, CounterType.FIRST_STRIKE))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        copyKeywordAsCounter(Keyword.DOUBLE_STRIKE, CounterType.DOUBLE_STRIKE))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        copyKeywordAsCounter(Keyword.DEATHTOUCH, CounterType.DEATHTOUCH))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        copyKeywordAsCounter(Keyword.INDESTRUCTIBLE, CounterType.INDESTRUCTIBLE))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, copyKeywordAsCounter(Keyword.LIFELINK, CounterType.LIFELINK))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, copyKeywordAsCounter(Keyword.MENACE, CounterType.MENACE))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, copyKeywordAsCounter(Keyword.REACH, CounterType.REACH))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, copyKeywordAsCounter(Keyword.TRAMPLE, CounterType.TRAMPLE))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        copyKeywordAsCounter(Keyword.VIGILANCE, CounterType.VIGILANCE))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.HASTE, CounterType.HASTE))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.FLYING, CounterType.FLYING))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.FIRST_STRIKE, CounterType.FIRST_STRIKE))
                .addEffect(EffectSlot.ON_ATTACK,
                        copyKeywordAsCounter(Keyword.DOUBLE_STRIKE, CounterType.DOUBLE_STRIKE))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.DEATHTOUCH, CounterType.DEATHTOUCH))
                .addEffect(EffectSlot.ON_ATTACK,
                        copyKeywordAsCounter(Keyword.INDESTRUCTIBLE, CounterType.INDESTRUCTIBLE))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.LIFELINK, CounterType.LIFELINK))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.MENACE, CounterType.MENACE))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.REACH, CounterType.REACH))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.TRAMPLE, CounterType.TRAMPLE))
                .addEffect(EffectSlot.ON_ATTACK, copyKeywordAsCounter(Keyword.VIGILANCE, CounterType.VIGILANCE));
    }

    private static CardEffect copyKeywordAsCounter(Keyword keyword, CounterType counterType) {
        return new ConditionalEffect(
                new TargetPermanentMatches(new PermanentHasKeywordPredicate(keyword)),
                new ConditionalEffect(new NotCondition(new SelfHasKeyword(keyword)),
                        PutCountersOnSelfEffect.targeted(counterType), false), false);
    }
}
