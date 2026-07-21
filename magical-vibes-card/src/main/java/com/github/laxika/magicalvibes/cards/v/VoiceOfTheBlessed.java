package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "INR", collectorNumber = "50")
public class VoiceOfTheBlessed extends Card {

    public VoiceOfTheBlessed() {
        // Whenever you gain life, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutCountersOnSourceEffect(1, 1, 1));

        // As long as this creature has four or more +1/+1 counters on it, it has flying and vigilance.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(4, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(4, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)));

        // As long as this creature has ten or more +1/+1 counters on it, it has indestructible.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
    }
}
