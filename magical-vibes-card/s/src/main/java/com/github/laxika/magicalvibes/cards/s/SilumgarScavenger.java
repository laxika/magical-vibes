package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceExploitedTriggeringPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "107")
@CardRegistration(set = "2XM", collectorNumber = "105")
public class SilumgarScavenger extends Card {

    public SilumgarScavenger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                new ConditionalEffect(
                new SourceExploitedTriggeringPermanent(),
                new GrantKeywordEffect(Keyword.HASTE, Subjects.source())));
    }
}
