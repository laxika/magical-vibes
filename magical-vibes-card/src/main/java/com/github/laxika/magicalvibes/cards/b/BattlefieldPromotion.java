package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "5")
public class BattlefieldPromotion extends Card {

    public BattlefieldPromotion() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
