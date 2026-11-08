package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "144")
@CardRegistration(set = "IMA", collectorNumber = "220")
@CardRegistration(set = "SLD", collectorNumber = "2186")
@CardRegistration(set = "SLD", collectorNumber = "2456")
public class Mindcrank extends Card {

    public Mindcrank() {
        addTrigger(Triggers.opponentLosesLife(), new MillEffect(new EventValue(), MillRecipient.TRIGGERING_PLAYER));
    }
}
