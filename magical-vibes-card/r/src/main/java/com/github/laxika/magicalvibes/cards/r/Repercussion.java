package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "95")
@CardRegistration(set = "SLD", collectorNumber = "1065")
@CardRegistration(set = "WOT", collectorNumber = "48")
@CardRegistration(set = "WOT", collectorNumber = "78")
@CardRegistration(set = "WOT", collectorNumber = "98")
public class Repercussion extends Card {

    public Repercussion() {
        addTrigger(Triggers.creatureDealtDamage(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
