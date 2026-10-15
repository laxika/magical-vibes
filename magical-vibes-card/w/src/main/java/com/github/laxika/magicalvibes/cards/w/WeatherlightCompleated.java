package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "242")
public class WeatherlightCompleated extends Card {

    public WeatherlightCompleated() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(4, CounterType.PHYRESIS),
                new AnimatePermanentsEffect(
                        (DynamicAmount) null,
                        (DynamicAmount) null,
                        List.of(CardSubtype.PHYREXIAN),
                        Set.of(),
                        null,
                        Set.of(CardType.CREATURE),
                        GrantScope.SELF,
                        EffectDuration.CONTINUOUS,
                        null
                )
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PHYRESIS),
                new ConditionalReplacementEffect(
                        new SourceCounterThreshold(7, CounterType.PHYRESIS),
                        new ScryEffect(1),
                        new DrawCardEffect(1)
                )
        ));
    }
}
