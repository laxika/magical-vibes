package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "146")
public class AangALotToLearn extends Card {

    public AangALotToLearn() {
        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.LESSON)),
                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.source())));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
