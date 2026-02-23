package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MetalcraftKeywordEffect;

@CardRegistration(set = "SOM", collectorNumber = "3")
public class AuriokEdgewright extends Card {

    public AuriokEdgewright() {
        addEffect(EffectSlot.STATIC, new MetalcraftKeywordEffect(Keyword.DOUBLE_STRIKE));
    }
}
