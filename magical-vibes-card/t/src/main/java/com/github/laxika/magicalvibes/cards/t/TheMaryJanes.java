package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "SPE", collectorNumber = "15")
public class TheMaryJanes extends Card {

    public TheMaryJanes() {
        addStaticEffect(new ReduceOwnCastCostEffect(new CreaturesAttackedThisTurn(CountScope.ANY_PLAYER)));
    }
}
