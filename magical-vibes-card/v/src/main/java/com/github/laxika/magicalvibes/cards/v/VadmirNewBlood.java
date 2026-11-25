package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "113")
public class VadmirNewBlood extends Card {

    public VadmirNewBlood() {
        addTrigger(Triggers.youCommitCrime(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(4, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Set.of(Keyword.MENACE, Keyword.LIFELINK), Subjects.source())));
    }
}
