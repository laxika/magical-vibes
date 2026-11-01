package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceIfFirstResolutionThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "192")
public class TeethingWurmlet extends Card {

    public TeethingWurmlet() {
        addStaticEffect(new ConditionalEffect(new Metalcraft(),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)));
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL), SequenceEffect.of(
                new GainLifeEffect(1),
                new PutCountersOnSourceIfFirstResolutionThisTurnEffect(
                        "artifact-enters", CounterType.PLUS_ONE_PLUS_ONE, 1)));
    }
}
