package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EquippedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SOM", collectorNumber = "23")
public class SunspearShikari extends Card {

    public SunspearShikari() {
        addEffect(EffectSlot.STATIC, new EquippedConditionalEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new EquippedConditionalEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)));
    }
}
