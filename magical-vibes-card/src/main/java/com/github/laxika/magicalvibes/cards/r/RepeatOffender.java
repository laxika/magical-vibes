package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSuspected;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "101")
public class RepeatOffender extends Card {

    public RepeatOffender() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new ConditionalEffect(
                                new SourceIsSuspected(),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                        ),
                        new ConditionalEffect(
                                new NotCondition(new SourceIsSuspected()),
                                new SuspectEffect(GrantScope.SELF)
                        )
                ),
                "{2}{B}: If this creature is suspected, put a +1/+1 counter on it. Otherwise, suspect it."
        ));
    }
}
