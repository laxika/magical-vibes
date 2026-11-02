package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "79")
@CardRegistration(set = "TSR", collectorNumber = "142")
public class StrongholdRats extends Card {

    public StrongholdRats() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));
    }
}
