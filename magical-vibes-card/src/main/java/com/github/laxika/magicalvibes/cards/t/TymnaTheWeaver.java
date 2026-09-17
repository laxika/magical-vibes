package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsDealtCombatDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndDrawEqualToAmountEffect;

@CardRegistration(set = "FCA", collectorNumber = "18")
public class TymnaTheWeaver extends Card {

    public TymnaTheWeaver() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED,
                new MayPayLifeAndDrawEqualToAmountEffect(new OpponentsDealtCombatDamageThisTurn()));
    }
}
