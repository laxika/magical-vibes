package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "5")
public class CaptainAmericaTeamLeader extends Card {

    public CaptainAmericaTeamLeader() {
        var hero = new PermanentHasSubtypePredicate(CardSubtype.HERO);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, hero)),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        new PutCountersEffect(
                                Subjects.triggering(), CounterType.PLUS_ONE_PLUS_ONE),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.HASTE),
                                Subjects.triggering())));
    }
}
