package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "63a")
@CardRegistration(set = "ALL", collectorNumber = "63b")
@CardRegistration(set = "TSB", collectorNumber = "49")
public class SwampMosquito extends Card {

    public SwampMosquito() {
        // Whenever this creature attacks and isn't blocked, defending player gets a poison counter.
        addTrigger(Triggers.attacksUnblocked(), new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));
    }
}
