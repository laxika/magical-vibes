package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "153")
public class GrinningDemon extends Card {

    public GrinningDemon() {
        addMorph("{2}{B}{B}");
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new LoseLifeEffect(2));
    }
}
