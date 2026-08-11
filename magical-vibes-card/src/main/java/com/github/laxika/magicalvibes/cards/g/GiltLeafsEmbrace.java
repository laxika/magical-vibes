package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "177")
public class GiltLeafsEmbrace extends Card {

    public GiltLeafsEmbrace() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE), GrantScope.TARGET))
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.ENCHANTED_CREATURE));
    }
}
