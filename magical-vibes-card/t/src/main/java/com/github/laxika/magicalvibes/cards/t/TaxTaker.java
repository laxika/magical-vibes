package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "288")
@CardRegistration(set = "MB2", collectorNumber = "524")
public class TaxTaker extends Card {

    public TaxTaker() {
        addTrigger(Triggers.opponentPaysTax(),
                CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
