package com.github.laxika.magicalvibes.cards.f;

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

@CardRegistration(set = "ZNR", collectorNumber = "15")
public class FearlessFledgling extends Card {

    public FearlessFledgling() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.FLYING, Subjects.source())));
    }
}
