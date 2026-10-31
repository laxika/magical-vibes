package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
        addSpellEffect(new GrantKeywordEffect(Keyword.SHADOW, GrantScope.TARGET));
        addSpellEffect(new DrawCardEffect(1));
    }
}
