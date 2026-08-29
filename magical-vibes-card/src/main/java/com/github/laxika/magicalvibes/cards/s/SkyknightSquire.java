package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "FDN", collectorNumber = "23")
public class SkyknightSquire extends Card {

    public SkyknightSquire() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        SourceCounterThreshold threePlusOnePlusOneCounters =
                new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                threePlusOnePlusOneCounters,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                threePlusOnePlusOneCounters,
                new GrantSubtypeEffect(CardSubtype.KNIGHT, GrantScope.SELF)));
    }
}
