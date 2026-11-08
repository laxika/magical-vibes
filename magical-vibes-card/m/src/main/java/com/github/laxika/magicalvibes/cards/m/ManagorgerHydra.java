package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "186")
@CardRegistration(set = "SLD", collectorNumber = "306")
@CardRegistration(set = "MOC", collectorNumber = "307")
@CardRegistration(set = "C21", collectorNumber = "199")
@CardRegistration(set = "BLC", collectorNumber = "230")
@CardRegistration(set = "C16", collectorNumber = "157")
@CardRegistration(set = "FDC", collectorNumber = "216")
public class ManagorgerHydra extends Card {

    public ManagorgerHydra() {
        // Whenever a player casts a spell, put a +1/+1 counter on this creature.
        addTrigger(Triggers.playerCastsSpell().anySpell(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
