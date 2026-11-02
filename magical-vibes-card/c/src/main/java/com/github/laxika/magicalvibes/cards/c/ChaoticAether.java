package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BlankPlanarDieRollsCauseChaosEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "1")
@CardRegistration(set = "OPCA", collectorNumber = "1")
@CardRegistration(set = "MOC", collectorNumber = "141")
public class ChaoticAether extends Card {

    public ChaoticAether() {
        addTrigger(Triggers.encounterThis(), new BlankPlanarDieRollsCauseChaosEffect());
    }
}
