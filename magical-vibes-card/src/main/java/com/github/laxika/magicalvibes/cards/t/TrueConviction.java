package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SOM", collectorNumber = "25")
public class TrueConviction extends Card {

    public TrueConviction() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES));
    }
}
