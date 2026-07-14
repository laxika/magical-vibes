package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "EVE", collectorNumber = "63")
public class Thunderblust extends Card {

    public Thunderblust() {
        // This creature has trample as long as it has a -1/-1 counter on it.
        // (Haste and Persist are keywords auto-loaded from Scryfall and handled by the engine.)
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
