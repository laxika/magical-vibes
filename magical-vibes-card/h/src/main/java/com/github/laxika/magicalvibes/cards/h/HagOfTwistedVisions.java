package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "H25")
public class HagOfTwistedVisions extends Card {

    public HagOfTwistedVisions() {
        // At the beginning of your upkeep, scry 2.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ScryEffect(2));
    }
}
