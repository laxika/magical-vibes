package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "150")
public class Quagnoth extends Card {

    public Quagnoth() {
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
