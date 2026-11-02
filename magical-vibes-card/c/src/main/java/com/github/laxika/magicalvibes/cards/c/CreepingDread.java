package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreepingDreadEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "104")
public class CreepingDread extends Card {

    public CreepingDread() {
        // At the beginning of your upkeep, each player discards a card. Each opponent who discarded
        // a card sharing a type with the card you discarded loses 3 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CreepingDreadEffect());
    }
}
