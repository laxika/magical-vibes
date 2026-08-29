package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

/** Evolved Spinoderm's oil-counter countdown and conditional evasion abilities. */
@CardRegistration(set = "ONE", collectorNumber = "166")
public class EvolvedSpinoderm extends Card {

    public EvolvedSpinoderm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.OIL, new Fixed(4)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new RemoveCounterFromSourceEffect(CounterType.OIL, 1),
                new ConditionalEffect(
                        new NotCondition(new SourceCounterThreshold(1, CounterType.OIL)),
                        new SacrificeSelfEffect())));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(3, CounterType.OIL)),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.OIL),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
    }
}
