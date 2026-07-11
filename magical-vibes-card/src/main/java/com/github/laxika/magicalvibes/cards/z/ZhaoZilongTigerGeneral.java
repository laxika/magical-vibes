package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "33")
public class ZhaoZilongTigerGeneral extends Card {

    public ZhaoZilongTigerGeneral() {
        // Horsemanship is auto-loaded from Scryfall keywords.
        // Whenever Zhao Zilong blocks, it gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(1, 1));
    }
}
