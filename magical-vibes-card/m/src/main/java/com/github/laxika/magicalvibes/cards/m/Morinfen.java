package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "75")
public class Morinfen extends Card {

    public Morinfen() {
        // Cumulative upkeep—Pay 1 life. (Flying is auto-loaded from Scryfall.)
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CumulativeUpkeepEffect.life(1));
    }
}
