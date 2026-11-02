package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenLandwalkToSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "76")
public class IllusionaryPresence extends Card {

    public IllusionaryPresence() {
        // Cumulative upkeep {U}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{U}"));

        // At the beginning of your upkeep, choose a land type. This creature gains landwalk of the
        // chosen type until end of turn.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new GrantChosenLandwalkToSelfEffect());
    }
}
