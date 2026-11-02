package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "32")
@CardRegistration(set = "OPCA", collectorNumber = "65")
public class QuicksilverSea extends Card {

    public QuicksilverSea() {
        // When you planeswalk to Quicksilver Sea and at the beginning of your upkeep, scry 4.
        addTrigger(Triggers.planeswalkToThis(), new ScryEffect(4));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ScryEffect(4));

        // Whenever chaos ensues, reveal the top card of your library. You may play it without
        // paying its mana cost. If it is not played, it stays on top of the library.
        addTrigger(Triggers.chaosEnsues(),
                new RevealTopCardMayPlayFreeEffect(LookDestination.TOP_OF_LIBRARY));
    }
}
