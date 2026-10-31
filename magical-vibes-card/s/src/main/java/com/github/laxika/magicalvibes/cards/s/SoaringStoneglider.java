package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardOrPayManaCost;

@CardRegistration(set = "SOS", collectorNumber = "32")
public class SoaringStoneglider extends Card {

    public SoaringStoneglider() {
        addCastingCost(new ExileNCardsFromGraveyardOrPayManaCost(2, "{1}{W}"));
    }
}
