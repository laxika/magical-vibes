package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "19")
@CardRegistration(set = "GK2", collectorNumber = "4")
public class StoicEphemera extends Card {

    public StoicEphemera() {
        addTrigger(Triggers.blocks(), new SacrificeAtEndOfCombatEffect());
    }
}
