package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ME1", collectorNumber = "154")
@CardRegistration(set = "2ED", collectorNumber = "239")
@CardRegistration(set = "LEA", collectorNumber = "238")
@CardRegistration(set = "LEB", collectorNumber = "239")
@CardRegistration(set = "SLZ", collectorNumber = "101")
@CardRegistration(set = "SLZ", collectorNumber = "222")
@CardRegistration(set = "SLZ", collectorNumber = "343")
public class CopperTablet extends Card {

    public CopperTablet() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DealDamageToPlayersEffect(1, DamageRecipient.ACTIVE_PLAYER));
    }
}
