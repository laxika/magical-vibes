package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FCA", collectorNumber = "50")
public class BruseTarlBoorishHerder extends Card {

    public BruseTarlBoorishHerder() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET))
                .addEffect(EffectSlot.ON_ATTACK,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET))
                .addEffect(EffectSlot.ON_ATTACK,
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET));
    }
}
