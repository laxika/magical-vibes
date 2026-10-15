package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;

@CardRegistration(set = "EOE", collectorNumber = "207")
public class Skystinger extends Card {

    public Skystinger() {
        // Reach keyword is auto-loaded from Scryfall.
        // Whenever this creature blocks a creature with flying, this creature gets +5/+0 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenBlockingKeywordEffect(Keyword.FLYING, 5, 0));
    }
}
