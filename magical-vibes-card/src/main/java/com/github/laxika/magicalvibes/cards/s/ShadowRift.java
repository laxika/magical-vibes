package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMP", collectorNumber = "86")
@CardRegistration(set = "TPR", collectorNumber = "66")
public class ShadowRift extends Card {

    public ShadowRift() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.SHADOW, GrantScope.TARGET));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
