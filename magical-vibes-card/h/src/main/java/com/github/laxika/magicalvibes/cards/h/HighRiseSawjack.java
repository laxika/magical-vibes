package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "150")
public class HighRiseSawjack extends Card {

    public HighRiseSawjack() {
        addTrigger(Triggers.blocks(), new BoostSelfWhenBlockingKeywordEffect(Keyword.FLYING, 2, 0));
    }
}
