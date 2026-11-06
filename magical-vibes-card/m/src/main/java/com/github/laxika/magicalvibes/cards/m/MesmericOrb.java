package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "204")
@CardRegistration(set = "2XM", collectorNumber = "272")
@CardRegistration(set = "BRR", collectorNumber = "31")
@CardRegistration(set = "SLD", collectorNumber = "2457")
public class MesmericOrb extends Card {

    public MesmericOrb() {
        addTrigger(Triggers.permanentBecomesUntapped(),
                new MillEffect(1, MillRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
