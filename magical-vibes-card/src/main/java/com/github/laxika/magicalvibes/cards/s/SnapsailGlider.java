package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;

@CardRegistration(set = "SOM", collectorNumber = "203")
public class SnapsailGlider extends Card {

    public SnapsailGlider() {
        addEffect(EffectSlot.STATIC, new MetalcraftConditionalEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
