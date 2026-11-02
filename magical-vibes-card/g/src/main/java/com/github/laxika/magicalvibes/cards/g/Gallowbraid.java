package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "70")
public class Gallowbraid extends Card {

    public Gallowbraid() {
        // Cumulative upkeep—Pay 1 life. (Trample is auto-loaded from Scryfall.)
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CumulativeUpkeepEffect.life(1));
    }
}
