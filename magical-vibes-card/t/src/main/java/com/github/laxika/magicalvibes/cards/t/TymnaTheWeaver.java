package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.OpponentsDealtCombatDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndDrawEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FCA", collectorNumber = "18")
@CardRegistration(set = "C16", collectorNumber = "48")
public class TymnaTheWeaver extends Card {

    public TymnaTheWeaver() {
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU),
                new MayPayLifeAndDrawEqualToAmountEffect(new OpponentsDealtCombatDamageThisTurn()));
    }
}
