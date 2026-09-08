package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DTK", collectorNumber = "24")
public class Lightwalker extends Card {

    public Lightwalker() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
