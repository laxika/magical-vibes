package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "136")
@CardRegistration(set = "DBL", collectorNumber = "136")
public class FalkenrathPerforator extends Card {

    public FalkenrathPerforator() {
        addTrigger(Triggers.attacks(), new DealDamageToPlayersEffect(1, DamageRecipient.DEFENDING_PLAYER));
    }
}
