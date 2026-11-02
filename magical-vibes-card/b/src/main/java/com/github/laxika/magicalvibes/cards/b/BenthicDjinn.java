package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "257")
public class BenthicDjinn extends Card {

    public BenthicDjinn() {
        // Islandwalk is a Scryfall-loaded keyword.
        // At the beginning of your upkeep, you lose 2 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new LoseLifeEffect(2));
    }
}
