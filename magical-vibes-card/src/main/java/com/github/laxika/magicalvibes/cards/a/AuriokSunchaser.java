package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MetalcraftKeywordEffect;

@CardRegistration(set = "SOM", collectorNumber = "4")
public class AuriokSunchaser extends Card {

    public AuriokSunchaser() {
        addEffect(EffectSlot.STATIC, new MetalcraftKeywordEffect(Keyword.FLYING, 2, 2));
    }
}
