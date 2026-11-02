package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "261")
public class RunawayCarriage extends Card {

    public RunawayCarriage() {
        addTrigger(Triggers.attacks(), new SacrificeAtEndOfCombatEffect());
        addTrigger(Triggers.blocks(), new SacrificeAtEndOfCombatEffect());
    }
}
