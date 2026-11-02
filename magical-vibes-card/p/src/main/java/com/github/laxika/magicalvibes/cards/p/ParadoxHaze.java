package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "71")
@CardRegistration(set = "SPG", collectorNumber = "123")
public class ParadoxHaze extends Card {

    public ParadoxHaze() {
        setEnchantPlayer(true);
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER), new AdditionalUpkeepStepEffect());
    }
}
