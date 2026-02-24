package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;

@CardRegistration(set = "SOM", collectorNumber = "120")
public class EzurisArchers extends Card {

    public EzurisArchers() {
        // Reach keyword is auto-loaded from Scryfall.
        // Whenever this creature blocks a creature with flying, this creature gets +3/+0 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenBlockingKeywordEffect(Keyword.FLYING, 3, 0));
    }
}
