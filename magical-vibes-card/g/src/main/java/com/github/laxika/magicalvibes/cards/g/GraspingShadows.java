package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ShadowsLair;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "LCI", collectorNumber = "108")
public class GraspingShadows extends Card {

    public GraspingShadows() {
        setBackFaceCard(new ShadowsLair());

        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), SequenceEffect.of(
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET),
                        new PutCountersOnSelfEffect(CounterType.DREAD),
                        new ConditionalEffect(
                                new SourceCounterThreshold(3, CounterType.DREAD),
                                new TransformSelfEffect()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "ShadowsLair";
    }
}
