package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

@CardRegistration(set = "SOS", collectorNumber = "32")
public class SoaringStoneglider extends Card {

    public SoaringStoneglider() {
        addCastingCost(AnyOfCost.of(new ExileNCardsFromGraveyardCost(2, null), new PayManaCost("{1}{W}")));
    }
}
