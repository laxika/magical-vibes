package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromHandMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "78")
public class ElkinLair extends Card {

    public ElkinLair() {
        // Triggers.upkeep(ActivePlayer.ANY) sets targetId to the active player. That player exiles a random
        // hand card, may play it this turn; unplayed → graveyard at next end step.
        // World rule is handled by SBA.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new ExileRandomCardFromHandMayPlayThisTurnEffect());
    }
}
