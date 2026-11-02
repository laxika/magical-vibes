package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "112")
@CardRegistration(set = "TSB", collectorNumber = "97")
@CardRegistration(set = "MM2", collectorNumber = "180")
@CardRegistration(set = "A25", collectorNumber = "208")
@CardRegistration(set = "SPG", collectorNumber = "35")
@CardRegistration(set = "C15", collectorNumber = "228")
public class MysticSnake extends Card {

    public MysticSnake() {
        // Flash is auto-loaded from Scryfall.

        // When this creature enters, counter target spell.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CounterSpellEffect());
    }
}
