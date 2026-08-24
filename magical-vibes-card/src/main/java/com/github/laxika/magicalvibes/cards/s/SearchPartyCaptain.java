package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "MID", collectorNumber = "32")
public class SearchPartyCaptain extends Card {

    public SearchPartyCaptain() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new CreaturesAttackedThisTurn(CountScope.CONTROLLER)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
    }
}
