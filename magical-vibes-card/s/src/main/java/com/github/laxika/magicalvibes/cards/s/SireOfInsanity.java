package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "104")
@CardRegistration(set = "SLD", collectorNumber = "275")
@CardRegistration(set = "PIO", collectorNumber = "242")
public class SireOfInsanity extends Card {

    public SireOfInsanity() {
        // At the beginning of each end step, each player discards their hand.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));
    }
}
