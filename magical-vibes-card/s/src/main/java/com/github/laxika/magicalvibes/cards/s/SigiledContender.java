package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

/** Sigiled Contender has lifelink while it has a +1/+1 counter. */
@CardRegistration(set = "M21", collectorNumber = "323")
public class SigiledContender extends Card {

    public SigiledContender() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)));
    }
}
