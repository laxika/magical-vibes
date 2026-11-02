package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "17")
@CardRegistration(set = "FIC", collectorNumber = "244")
@CardRegistration(set = "NCC", collectorNumber = "202")
@CardRegistration(set = "ONC", collectorNumber = "73")
public class GratefulApparition extends Card {

    public GratefulApparition() {
        addTrigger(Triggers.dealsCombatDamageToPlayerOrPlaneswalker(),
                new ProliferateEffect());
    }
}
