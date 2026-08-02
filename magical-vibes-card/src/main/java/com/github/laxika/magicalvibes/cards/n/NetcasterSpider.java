package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;

@CardRegistration(set = "M15", collectorNumber = "186")
public class NetcasterSpider extends Card {

    public NetcasterSpider() {
        // Reach keyword is auto-loaded from Scryfall.
        // Whenever this creature blocks a creature with flying, this creature gets +2/+0 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenBlockingKeywordEffect(Keyword.FLYING, 2, 0));
    }
}
