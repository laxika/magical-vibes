package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "69")
@CardRegistration(set = "SLD", collectorNumber = "1405")
public class NecrogenMists extends Card {

    public NecrogenMists() {
        // At the beginning of each player's upkeep, that player discards a card.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new DiscardEffect(1, DiscardRecipient.ACTIVE_PLAYER));
    }
}
