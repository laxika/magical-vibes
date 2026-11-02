package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "119")
public class CoalhaulerSwine extends Card {

    public CoalhaulerSwine() {
        addTrigger(Triggers.dealtDamage(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_PLAYER));
    }
}
