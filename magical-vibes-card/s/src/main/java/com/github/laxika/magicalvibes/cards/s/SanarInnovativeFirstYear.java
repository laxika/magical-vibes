package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForVividEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "241")
@CardRegistration(set = "ECL", collectorNumber = "378")
public class SanarInnovativeFirstYear extends Card {

    public SanarInnovativeFirstYear() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new RevealTopCardsForVividEffect(new ColorsAmongControlledPermanents()));
    }
}
