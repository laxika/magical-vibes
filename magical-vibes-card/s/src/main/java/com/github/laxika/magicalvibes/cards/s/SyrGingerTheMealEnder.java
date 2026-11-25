package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "252")
@CardRegistration(set = "WOE", collectorNumber = "369")
public class SyrGingerTheMealEnder extends Card {

    public SyrGingerTheMealEnder() {
        addStaticEffect(new ConditionalEffect(
                new OpponentControlsPermanent(new PermanentIsPlaneswalkerPredicate()),
                new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HEXPROOF, Keyword.HASTE), Subjects.source())));

        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE), new ScryEffect(1)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(true), new GainLifeEffect(new XValue())),
                "{2}, {T}, Sacrifice Syr Ginger: You gain life equal to its power."
        ));
    }
}
