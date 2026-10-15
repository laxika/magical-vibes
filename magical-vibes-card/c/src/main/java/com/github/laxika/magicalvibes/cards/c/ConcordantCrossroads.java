package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "CHR", collectorNumber = "60")
@CardRegistration(set = "LEG", collectorNumber = "179")
@CardRegistration(set = "ME3", collectorNumber = "114")
@CardRegistration(set = "SLD", collectorNumber = "467")
@CardRegistration(set = "SLD", collectorNumber = "2073")
@CardRegistration(set = "2X2", collectorNumber = "141")
public class ConcordantCrossroads extends Card {

    public ConcordantCrossroads() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_CREATURES_INCLUDING_SELF));
    }
}
