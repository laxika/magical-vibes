package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "215")
public class ThunderingDjinn extends Card {

    public ThunderingDjinn() {
        // Whenever this creature attacks, it deals damage to any target equal to the number
        // of cards you've drawn this turn.
        addTrigger(Triggers.attacks(), new DealDamageToAnyTargetEffect(new CardsDrawnThisTurn()));
    }
}
