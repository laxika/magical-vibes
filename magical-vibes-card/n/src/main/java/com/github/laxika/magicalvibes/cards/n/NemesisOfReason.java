package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "28")
@CardRegistration(set = "SLD", collectorNumber = "1302")
public class NemesisOfReason extends Card {

    public NemesisOfReason() {
        // Whenever Nemesis of Reason attacks, defending player mills ten cards.
        addTrigger(Triggers.attacks(), new MillDefendingPlayerEffect(10));
    }
}
