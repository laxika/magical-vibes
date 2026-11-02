package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "117")
public class ShinkaGatekeeper extends Card {

    public ShinkaGatekeeper() {
        addTrigger(Triggers.dealtDamage(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CONTROLLER));
    }
}
