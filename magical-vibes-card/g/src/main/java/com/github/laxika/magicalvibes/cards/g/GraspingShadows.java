package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ShadowsLair;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "108")
public class GraspingShadows extends Card {

    public GraspingShadows() {
        setBackFaceCard(new ShadowsLair());

        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                SequenceEffect.of(
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.target()),
                        new GrantKeywordEffect(Keyword.LIFELINK, Subjects.target()),
                        PutCountersEffect.onSelf(CounterType.DREAD),
                        new ConditionalEffect(
                                new SourceCounterThreshold(3, CounterType.DREAD),
                                new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "ShadowsLair";
    }
}
