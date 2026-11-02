package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "217")
@CardRegistration(set = "GK1", collectorNumber = "113")
@CardRegistration(set = "PIO", collectorNumber = "308")
@CardRegistration(set = "SLD", collectorNumber = "2444")
@CardRegistration(set = "C19", collectorNumber = "193")
public class GrowingRanks extends Card {

    public GrowingRanks() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PopulateEffect());
    }
}
