package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.HullbreakerHorrorTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "68")
@CardRegistration(set = "INR", collectorNumber = "303")
@CardRegistration(set = "INR", collectorNumber = "357")
@CardRegistration(set = "VOW", collectorNumber = "63")
@CardRegistration(set = "DBL", collectorNumber = "330")
@CardRegistration(set = "SLZ", collectorNumber = "19")
@CardRegistration(set = "SLZ", collectorNumber = "140")
@CardRegistration(set = "SLZ", collectorNumber = "261")
@CardRegistration(set = "PIP", collectorNumber = "344")
@CardRegistration(set = "PIP", collectorNumber = "872")
@CardRegistration(set = "SLD", collectorNumber = "2321")
@CardRegistration(set = "SLD", collectorNumber = "2336")
@CardRegistration(set = "WOC", collectorNumber = "95")
public class HullbreakerHorror extends Card {

    public HullbreakerHorror() {
        // Flash — keyword from Scryfall.
        // This spell can't be countered.
        addStaticEffect(new CantBeCounteredEffect());
        // Whenever you cast a spell, choose up to one —
        // • Return target spell you don't control to its owner's hand.
        // • Return target nonland permanent to its owner's hand.
        addTrigger(Triggers.youCastSpell().anySpell(), new HullbreakerHorrorTriggerEffect());
    }
}
