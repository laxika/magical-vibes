package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageBySelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;

@CardRegistration(set = "CSP", collectorNumber = "25")
public class WoollyRazorback extends Card {

    public WoollyRazorback() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.ICE, new Fixed(3)));

        SourceCounterThreshold hasIceCounter = new SourceCounterThreshold(1, CounterType.ICE);
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(hasIceCounter,
                        new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(hasIceCounter, new PreventAllCombatDamageBySelfEffect()));

        addEffect(EffectSlot.ON_BLOCK, new RemoveCounterFromSourceEffect(CounterType.ICE, 1));
    }
}
