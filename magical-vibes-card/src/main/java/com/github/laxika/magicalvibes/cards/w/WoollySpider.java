package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;

@CardRegistration(set = "ICE", collectorNumber = "279")
public class WoollySpider extends Card {

    public WoollySpider() {
        // Reach keyword is auto-loaded from Scryfall.
        // Whenever this creature blocks a creature with flying, this creature gets +0/+2 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenBlockingKeywordEffect(Keyword.FLYING, 0, 2));
    }
}
