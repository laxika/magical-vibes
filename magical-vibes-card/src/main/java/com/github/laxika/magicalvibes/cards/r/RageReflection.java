package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SHM", collectorNumber = "104")
public class RageReflection extends Card {

    public RageReflection() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.OWN_CREATURES));
    }
}
