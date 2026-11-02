package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "183")
@CardRegistration(set = "PD2", collectorNumber = "2")
@CardRegistration(set = "A25", collectorNumber = "139")
public class JackalPup extends Card {

    public JackalPup() {
        // Whenever this creature is dealt damage, it deals that much damage to you.
        // The damage amount snapshots onto the trigger entry's eventValue.
        addTrigger(Triggers.dealtDamage(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CONTROLLER));
    }
}
