package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MB1", collectorNumber = "71")
public class BearWithSetsMechanic extends Card {

    public BearWithSetsMechanic() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.AGGRESSIVE, GrantScope.SELF));
    }
}
