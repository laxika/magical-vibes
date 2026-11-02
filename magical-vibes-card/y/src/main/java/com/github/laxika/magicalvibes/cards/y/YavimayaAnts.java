package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "105")
@CardRegistration(set = "DKM", collectorNumber = "32")
@CardRegistration(set = "ME1", collectorNumber = "140")
public class YavimayaAnts extends Card {

    public YavimayaAnts() {
        // Trample and haste are keywords loaded from Scryfall.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{G}{G}"));
    }
}
