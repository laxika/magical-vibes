package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "184")
public class FelotharDawnOfTheAbzan extends Card {

    public FelotharDawnOfTheAbzan() {
        CardEffect sacrificeThenCounters = new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentNotPredicate(new PermanentIsLandPredicate()), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())))),
                "Sacrifice a nonland permanent?");
        addTrigger(Triggers.enters(ObjectRelation.THIS), sacrificeThenCounters);
        addTrigger(Triggers.attacks(), sacrificeThenCounters);
    }
}
