package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "18")
public class Lifelink extends Card {

    public Lifelink() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.ENCHANTED_CREATURE));
    }
}
