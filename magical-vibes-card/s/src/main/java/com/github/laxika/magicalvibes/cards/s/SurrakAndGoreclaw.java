package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "337")
@CardRegistration(set = "MOM", collectorNumber = "380")
@CardRegistration(set = "FDC", collectorNumber = "231")
public class SurrakAndGoreclaw extends Card {

    public SurrakAndGoreclaw() {
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.otherCreaturesYouControl()));
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        PutCountersEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new GrantKeywordEffect(Keyword.HASTE, Subjects.triggering())));
    }
}
