package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "BLC", collectorNumber = "173")
@CardRegistration(set = "WOE", collectorNumber = "312")
public class RowdyResearch extends Card {

    public RowdyResearch() {
        // This spell costs {1} less to cast for each creature that attacked this turn.
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new CreaturesAttackedThisTurn(CountScope.ANY_PLAYER)));

        // Draw three cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
